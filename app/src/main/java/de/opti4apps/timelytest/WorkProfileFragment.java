package de.opti4apps.timelytest;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.Duration;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.WorkProfile;
import de.opti4apps.timelytest.data.WorkProfile_;
import de.opti4apps.timelytest.event.DurationPickedEvent;
import de.opti4apps.timelytest.event.TimePickedEvent;
import de.opti4apps.timelytest.event.WorkingProfileDatasetChangedEvent;
import de.opti4apps.timelytest.shared.DurationPickerFragment;
import io.objectbox.Box;
import io.objectbox.query.Query;

import static android.util.JsonToken.NULL;

/**
 * Created by TCHATCHO on 23.04.2017.
 */

public class WorkProfileFragment extends Fragment {

    public static final String TAG = WorkProfileFragment.class.getSimpleName();
    private static final String ARG_WORK_PROFILE_ID = "workProfileID";
    private static final String ARG_USER_ID = "userID";

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
        if (v.getId() == R.id.monTimeText) {
            DurationPickerFragment newFragment = new DurationPickerFragment();
            newFragment.setDay("monday");
            newFragment.show(getFragmentManager(), "durationMon");
        } else if (v.getId() == R.id.tuesTimeText) {
            DurationPickerFragment newFragment = new DurationPickerFragment();
            newFragment.setDay("tuesday");
            newFragment.show(getFragmentManager(), "durationTues");
        } else if (v.getId() == R.id.wedTimeText){
            DurationPickerFragment newFragment = new DurationPickerFragment();
            newFragment.setDay("wednesday");
            newFragment.show(getFragmentManager(), "durationWed");
        } else if (v.getId() == R.id.thursTimeText){
            DurationPickerFragment newFragment = new DurationPickerFragment();
            newFragment.setDay("thursday");
            newFragment.show(getFragmentManager(), "durationThurs");
        } else if (v.getId() == R.id.friTimeText){
            DurationPickerFragment newFragment = new DurationPickerFragment();
            newFragment.setDay("friday");
            newFragment.show(getFragmentManager(), "durationFri");
        }

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
            switch (event.day) {
                case "monday":
                    mWorkProfile.setMonWorkHours(Duration.millis(event.duration));
                    break;
                case "tuesday":
                    mWorkProfile.setTuesWorkHours(Duration.millis(event.duration));
                    break;
                case "wednesday":
                    mWorkProfile.setWedWorkHours(Duration.millis(event.duration));
                    break;
                case "thursday":
                    mWorkProfile.setThursWorkHours(Duration.millis(event.duration));
                    break;
                case "friday":
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
