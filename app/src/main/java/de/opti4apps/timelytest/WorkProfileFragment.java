package de.opti4apps.timelytest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.WorkProfile;
import de.opti4apps.timelytest.data.WorkProfile_;
import de.opti4apps.timelytest.event.DurationPickedEvent;
import de.opti4apps.timelytest.event.WorkingProfileDatasetChangedEvent;
import de.opti4apps.timelytest.shared.DurationPickerFragment;
import io.objectbox.Box;
import io.objectbox.query.Query;

/**
 * Created by TCHATCHO on 23.04.2017.
 */

public class WorkProfileFragment extends Fragment {

    public static final String TAG = WorkProfileFragment.class.getSimpleName();
    private static final String ARG_WORK_PROFILE_ID = "workProfileID";
    private static final String ARG_USER_ID = "userID";

    @BindView(R.id.monTimeLabel)
    TextView mMonLabel;

    @BindView(R.id.tuesTimeLabel)
    TextView mTueLabel;

    @BindView(R.id.wedTimeLabel)
    TextView mWedLabel;

    @BindView(R.id.thursTimeLabel)
    TextView mThursLabel;

    @BindView(R.id.friTimeLabel)
    TextView mFriLabel;

    @BindView(R.id.monTimeText)
    TextView mMonWorkHours;

    @BindView(R.id.tuesTimeText)
    TextView mTuesWorkHours;

    @BindView(R.id.wedTimeText)
    TextView mWedWorkHours;

    @BindView(R.id.thursTimeText)
    TextView mThursWorkHours;

    @BindView(R.id.friTimeText)
    TextView mFriWorkHours;

    @BindView(R.id.totalWorkingHours)
    TextView mtotalWorkHours;

    int mSelectedText;

    private WorkProfile mWorkProfile;
    private Box<WorkProfile> mWorkProfileBox;
    private Query<WorkProfile> mWorkProfileQuery;

    public WorkProfileFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userID the ID of the current user.
     * @return A new instance of fragment WorkProfileFragment.
     */
    public static WorkProfileFragment newInstance(long userID) {
        WorkProfileFragment fragment = new WorkProfileFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWorkProfileBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(WorkProfile.class);


        if (getArguments() != null) {
            // long workProfileID = getArguments().getLong(ARG_WORK_PROFILE_ID); in case we will want to save the previous workprofile
            long userID = getArguments().getLong(ARG_USER_ID);
            mWorkProfileQuery = mWorkProfileBox.query().equal(WorkProfile_.userID, userID).build();
            mWorkProfile = mWorkProfileQuery.findUnique();

            if (mWorkProfile == null){
                WorkProfile wp = new WorkProfile(userID, Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0));
                mWorkProfileBox.put(wp);

                mWorkProfileQuery = mWorkProfileBox.query().equal(WorkProfile_.userID, userID).build();
                mWorkProfile = mWorkProfileQuery.findUnique();
            }
            setRetainInstance(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.fragment_work_profile, container, false);
        ButterKnife.bind(this, view);
        DateTime dt = DateTime.parse("2017-05-08");
        mMonLabel.setText(dt.dayOfWeek().getAsShortText());
        dt = dt.plusHours(24);
        mTueLabel.setText(dt.dayOfWeek().getAsShortText());
        dt = dt.plusHours(24);
        mWedLabel.setText(dt.dayOfWeek().getAsShortText());
        dt = dt.plusHours(24);
        mThursLabel.setText(dt.dayOfWeek().getAsShortText());
        dt = dt.plusHours(24);
        mFriLabel.setText(dt.dayOfWeek().getAsShortText());
       updateUI();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @OnClick({R.id.monTimeText, R.id.tuesTimeText,R.id.wedTimeText, R.id.thursTimeText,R.id.friTimeText})
    public void showTimePickerDialog(View v) {
        mSelectedText = v.getId();
            DurationPickerFragment newFragment = new DurationPickerFragment();
            newFragment.show(getFragmentManager(), "durationMon");


    }

    @Subscribe
    public void onWorkingProfileDatasetChangedEvent(WorkingProfileDatasetChangedEvent event) {
        updateUI();
    }

    private void updateUI() {
        setmonTimeText();
        settuesTimeText();
        setwedTimeText();
        setthursTimeText();
        setfriTimeText();
        setTotalTime();
    }

    @Subscribe
    public void onDurationPickedEvent(DurationPickedEvent event) {
        switch (mSelectedText) {
            case R.id.monTimeText:
                    mWorkProfile.setMonWorkHours(Duration.millis(event.duration));
                    break;
            case R.id.tuesTimeText:
                    mWorkProfile.setTuesWorkHours(Duration.millis(event.duration));
                    break;
            case R.id.wedTimeText:
                    mWorkProfile.setWedWorkHours(Duration.millis(event.duration));
                    break;
            case R.id.thursTimeText:
                    mWorkProfile.setThursWorkHours(Duration.millis(event.duration));
                    break;
            case R.id.friTimeText:
                    mWorkProfile.setFriWorkHours(Duration.millis(event.duration));
                    break;
            }
        updateWorkingWeekhours();
    }


    private void updateWorkingWeekhours() {
        try {
            if (mWorkProfile.isValid()) {
                mWorkProfileBox.put(mWorkProfile);
                EventBus.getDefault().post(new WorkingProfileDatasetChangedEvent(TAG));
            }
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            int res = Integer.decode(msg);
            updateOnError(res);
            String message = getResources().getString(res);
            Log.d(TAG, res + " " + message);
            Toast.makeText(getActivity(), message,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateOnError(int res) {

    }

    public void setmonTimeText()
    {
        String monHours = mWorkProfile.getMonWorkHours().toPeriod().toString(Day.PERIOD_FORMATTER);
        mMonWorkHours.setText(monHours);
    }
    public void settuesTimeText()
    {
        String tuesHours = mWorkProfile.getTuesWorkHours().toPeriod().toString(Day.PERIOD_FORMATTER);
        mTuesWorkHours.setText(tuesHours);
    }
    public void setwedTimeText()
    {
        String wedHours = mWorkProfile.getWedWorkHours().toPeriod().toString(Day.PERIOD_FORMATTER);
        mWedWorkHours.setText(wedHours);
    }
    public void setthursTimeText()
    {
        String thursHours = mWorkProfile.getThursWorkHours().toPeriod().toString(Day.PERIOD_FORMATTER);
        mThursWorkHours.setText(thursHours);
    }
    public void setfriTimeText()
    {
        String friHours = mWorkProfile.getFriWorkHours().toPeriod().toString(Day.PERIOD_FORMATTER);
        mFriWorkHours.setText(friHours);
    }

    public void setTotalTime()
    {
        String totalHours = mWorkProfile.getTotalWorkingTime().toPeriod().toString(Day.PERIOD_FORMATTER);
        mtotalWorkHours.setText(totalHours);
    }

}
