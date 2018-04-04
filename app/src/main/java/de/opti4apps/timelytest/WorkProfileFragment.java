package de.opti4apps.timelytest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.WorkProfile;
import de.opti4apps.timelytest.event.DurationPickedEvent;
import de.opti4apps.timelytest.event.WorkingProfileDatasetChangedEvent;
import de.opti4apps.timelytest.shared.DurationPickerFragment;
import de.opti4apps.timelytest.shared.TimelyHelper;
import de.opti4apps.timelytest.shared.TrackerHelper;
import de.opti4apps.tracker.gesture.GestureTracker;
import io.objectbox.Box;
import io.objectbox.query.Query;

/**
 * Created by TCHATCHO on 23.04.2017.
 */

public class WorkProfileFragment extends Fragment {

    public static final String TAG = WorkProfileFragment.class.getSimpleName();
    private static final String ARG_WORK_PROFILE_ID = "workProfileID";
    private static final String ARG_USER_ID = "userID";
    private TrackerHelper tracker;

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

    @BindView(R.id.wokingHoursLabel)
    TextView mCurrentWorkingHoursMonth;

    @BindView(R.id.previousMonthOvertimeText)
    TextView mPrevOvertimeHours;

    int mSelectedText;

    private WorkProfile mWorkProfile;
    private WorkProfile mWorkProfileBoxed;
    private Box<WorkProfile> mWorkProfileBox;
    private Box<Day> mDayBox;
    private Query<WorkProfile> mWorkProfileQuery;

    long userID;

    private ActionMode mActionMode;
    private ActionModeCallback mActionModeCallback = new ActionModeCallback();

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
        tracker = new TrackerHelper(TAG,getContext());

        if (getArguments() != null) {
            userID = getArguments().getLong(ARG_USER_ID);
            DateTime currentMonth = new DateTime().dayOfMonth().withMinimumValue();
            mDayBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Day.class);
            mWorkProfileBoxed = TimelyHelper.getValidWorkingProfile(currentMonth, mWorkProfileBox, mDayBox);
            if(mWorkProfileBoxed == null) {
                    mWorkProfile = new WorkProfile(userID, Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0));
                }
            else{
                    mWorkProfile = new WorkProfile(mWorkProfileBoxed);
            }
            setRetainInstance(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_work_profile, container, false);
        ButterKnife.bind(this, view);

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                GestureTracker.trackGesture(getContext(),event,(ViewGroup) v);
                return true;
            }
        });

        String currentMonth = Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        mCurrentWorkingHoursMonth.setText(currentMonth + " Weekly Working Hours");


        ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);

        updateUI();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        tracker.onStartTrack();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        tracker.onStopTrack();
    }

    @OnClick({R.id.monTimeText, R.id.tuesTimeText,R.id.wedTimeText, R.id.thursTimeText,R.id.friTimeText, R.id.previousMonthOvertimeText})
    public void showTimePickerDialog(View v) {
        mSelectedText = v.getId();
        if (mSelectedText == R.id.monTimeText)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.monTimeText), tracker.getInteractionClicID());
            DurationPickerFragment newFragment = DurationPickerFragment.newInstance(mWorkProfile.getMonWorkHours().getMillis());
            newFragment.show(getFragmentManager(), "durationMon");
        }
        else if (mSelectedText == R.id.tuesTimeText)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.tuesTimeText), tracker.getInteractionClicID());
            DurationPickerFragment newFragment = DurationPickerFragment.newInstance(mWorkProfile.getTuesWorkHours().getMillis());
            newFragment.show(getFragmentManager(), "durationTue");
        }
        else if (mSelectedText == R.id.wedTimeText)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.wedTimeText), tracker.getInteractionClicID());
            DurationPickerFragment newFragment = DurationPickerFragment.newInstance(mWorkProfile.getWedWorkHours().getMillis());
            newFragment.show(getFragmentManager(), "durationWed");
        }
        else if (mSelectedText == R.id.thursTimeText)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.thursTimeText), tracker.getInteractionClicID());
            DurationPickerFragment newFragment = DurationPickerFragment.newInstance(mWorkProfile.getThursWorkHours().getMillis());
            newFragment.show(getFragmentManager(), "durationThu");
        }
        else if (mSelectedText == R.id.friTimeText)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.friTimeText), tracker.getInteractionClicID());
            DurationPickerFragment newFragment = DurationPickerFragment.newInstance(mWorkProfile.getFriWorkHours().getMillis());
            newFragment.show(getFragmentManager(), "durationFri");
        }
        else if (mSelectedText == R.id.previousMonthOvertimeText)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.previousMonthOvertimeText), tracker.getInteractionClicID());
            DurationPickerFragment newFragment = DurationPickerFragment.newInstance(mWorkProfile.getPreviousOvertime().getMillis());
            newFragment.show(getFragmentManager(), "durationPrevOvertime");
        }
    }

    @OnClick({R.id.monTimeLabel, R.id.tuesTimeLabel,R.id.wedTimeLabel, R.id.thursTimeLabel,R.id.friTimeLabel,
            R.id.previousMonthOvertimeLabel,R.id.totalWorkingHours,R.id.imageWatch,R.id.titleViewText})
    public void clickUnEditableLabelsImages(View v) {
        mSelectedText = v.getId();
        if (mSelectedText == R.id.monTimeLabel)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.monTimeLabel), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.tuesTimeLabel)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.tuesTimeLabel), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.wedTimeLabel)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.wedTimeLabel), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.thursTimeLabel)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.thursTimeLabel), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.friTimeLabel)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.friTimeLabel), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.previousMonthOvertimeLabel)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.previousMonthOvertimeLabel), tracker.getInteractionClicID());
        }
        else if ( mSelectedText == R.id.totalWorkingHours)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.totalWorkingHours), tracker.getInteractionClicID());
        }
        else if ( mSelectedText == R.id.imageWatch)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.imageWatch), tracker.getInteractionClicID());
        }
        else if ( mSelectedText == R.id.titleViewText)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.titleViewText), tracker.getInteractionClicID());
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
        setPrevOvertime();
    }

    @Subscribe
    public void onDurationPickedEvent(DurationPickedEvent event) {
        switch (mSelectedText) {
            case R.id.monTimeText:
                mWorkProfile.setMonWorkHours(Duration.millis(event.duration));
                tracker.interactionTrack(getActivity().findViewById(R.id.monTimeText), tracker.getInteractionEventID());
                break;
            case R.id.tuesTimeText:
                mWorkProfile.setTuesWorkHours(Duration.millis(event.duration));
                tracker.interactionTrack(getActivity().findViewById(R.id.tuesTimeText), tracker.getInteractionEventID());
                break;
            case R.id.wedTimeText:
                mWorkProfile.setWedWorkHours(Duration.millis(event.duration));
                tracker.interactionTrack(getActivity().findViewById(R.id.wedTimeText), tracker.getInteractionEventID());
                break;
            case R.id.thursTimeText:
                mWorkProfile.setThursWorkHours(Duration.millis(event.duration));
                tracker.interactionTrack(getActivity().findViewById(R.id.thursTimeText), tracker.getInteractionEventID());
                break;
            case R.id.friTimeText:
                mWorkProfile.setFriWorkHours(Duration.millis(event.duration));
                tracker.interactionTrack(getActivity().findViewById(R.id.friTimeText), tracker.getInteractionEventID());
                break;
            case R.id.previousMonthOvertimeText:
                mWorkProfile.setPreviousOvertime(Duration.millis(event.duration));
                tracker.interactionTrack(getActivity().findViewById(R.id.previousMonthOvertimeText), tracker.getInteractionEventID());
                break;
            }
        updateUI();
       // updateWorkingWeekhours();
    }


    private void updateWorkingWeekhours() {
        try {
            if (mWorkProfile.isValid()) {

                if(mWorkProfileBoxed == null){
                    mWorkProfileBoxed = new WorkProfile(mWorkProfile);
                    mWorkProfileBox.put(mWorkProfileBoxed);
                }
                else{
                    mWorkProfileBoxed.updateWorkingProfile(mWorkProfile);
                    mWorkProfileBox.put(mWorkProfileBoxed);
                }
               // EventBus.getDefault().post(new WorkingProfileDatasetChangedEvent(TAG));
                Toast.makeText(getActivity(), "Working profile saved", Toast.LENGTH_LONG).show();
            }
        } catch (IllegalArgumentException e) {
            if(mWorkProfileBoxed != null){
                mWorkProfile.updateWorkingProfile(mWorkProfileBoxed);
            }
            else {
                mWorkProfile = new WorkProfile(userID, Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0),
                        Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0));
            }
            String msg = e.getMessage();
            int res = Integer.decode(msg);
            updateOnError(res);
            String message = getResources().getString(res);
            Log.d(TAG, res + " " + message);
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            updateUI();
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

    public void setPrevOvertime()
    {
        String previousOvertime = mWorkProfile.getPreviousOvertime().toPeriod().toString(Day.PERIOD_FORMATTER);
        mPrevOvertimeHours.setText(previousOvertime);
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            mode.getMenuInflater().inflate(R.menu.capture_time_menu, menu);
            mActionMode.setTitle(getResources().getString(R.string.app_name));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.save:
                   // Toast.makeText(getActivity(), "SAVE", Toast.LENGTH_LONG).show();
                    tracker.interactionTrack(getActivity().findViewById(R.id.save), tracker.getInteractionClicID());
                    updateWorkingWeekhours();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getActivity().onBackPressed();
        }
    }
}
