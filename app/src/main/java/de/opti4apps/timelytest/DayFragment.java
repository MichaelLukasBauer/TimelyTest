package de.opti4apps.timelytest;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;


import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.Day_;
import de.opti4apps.timelytest.data.WorkProfile;
import de.opti4apps.timelytest.event.DatePickedEvent;
import de.opti4apps.timelytest.event.DayDatasetChangedEvent;
import de.opti4apps.timelytest.event.DurationPickedEvent;
import de.opti4apps.timelytest.event.TimePickedEvent;
import de.opti4apps.timelytest.shared.DatePickerFragment;
import de.opti4apps.timelytest.shared.DurationPickerFragment;
import de.opti4apps.timelytest.shared.TimePickerFragment;
import de.opti4apps.timelytest.shared.TimelyHelper;
import de.opti4apps.timelytest.shared.TrackerHelper;
import de.opti4apps.tracker.gesture.GestureTracker;
import io.objectbox.Box;
import io.objectbox.query.Query;


/**
 * A simple {@link Fragment} subclass representing .
 * Use the {@link DayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DayFragment extends Fragment {

    public static final String TAG = DayFragment.class.getSimpleName();
    private static final String ARG_DAY_ID = "dayID";
    private static final String ARG_USER_ID = "userID";
    private TrackerHelper tracker;

    @BindView(R.id.dateText)
    TextView mDate;

    @BindView(R.id.startTimeText)
    TextView mStart;

    @BindView(R.id.endTimeText)
    TextView mEnd;

    @BindView(R.id.pauseDurationText)
    TextView mPause;

    @BindView(R.id.dayOvertime)
    TextView mDayOvertime;

    @BindView(R.id.totalOvertime)
    TextView mTotalOvertime;

    @BindView(R.id.dayTypeSpinner)
    Spinner mSpinner;

    @BindView(R.id.workingHourstext)
    TextView mtotalWorkingHours;

    private Day mDay;
    private Box<Day> mDayBox;
    private WorkProfile mWorkProfile;
    private Box<WorkProfile> mWorkProfileBox;
    private Query<Day> mDayQuery;
    private Query<WorkProfile> mWorkProfileQuery;

    private ActionMode mActionMode;
    private ActionModeCallback mActionModeCallback = new ActionModeCallback();

    public DayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param dayID the ID of the day given to the fragment 0 if a new day should be created.
     * @return A new instance of fragment DayFragment.
     */
    public static DayFragment newInstance(long dayID,long userID) {
        DayFragment fragment = new DayFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DAY_ID, dayID);
        args.putLong(ARG_USER_ID, userID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        tracker = new TrackerHelper(TAG,getContext());
        mDayBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Day.class);
        mWorkProfileBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(WorkProfile.class);

        if (getArguments() != null) {
            long dayID = getArguments().getLong(ARG_DAY_ID);

            if(dayID == 0)
            {
                mDayQuery = mDayBox.query().equal (Day_.day, DateTime.now().toDate()).build();
                List<Day> days = mDayQuery.find();


                if (days.size() != 0)
                {
                    mDay = days.get(0);
                }
                else
                {
                    mDay = new Day(Day.DAY_TYPE.WORKDAY, DateTime.now(), DateTime.now(), DateTime.now(), Duration.standardMinutes(0));
                    mDay.setToDefaultDay();
                }
            }
            else
            {
                mDayQuery = mDayBox.query().equal(Day_.id, dayID).build();
                mDay = mDayQuery.findUnique();
            }

            getTheCurrentWorkingProfile();
        }
        setRetainInstance(true);
    }

    private void getTheCurrentWorkingProfile() {
        mWorkProfile = TimelyHelper.getValidWorkingProfileByDay(mDay,mWorkProfileBox, mDayBox);

        if (mWorkProfile == null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.error_finding_wp), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_day, container, false);
        ButterKnife.bind(this, view);

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                GestureTracker.trackGesture(getContext(),event,(ViewGroup) v);
                return true;
            }
        });

        final String[] dayTypes = getResources().getStringArray(R.array.day_type_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, dayTypes);
        mSpinner.setAdapter(adapter);

        updateUI();

        ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);

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

    @OnClick({R.id.startTimeText, R.id.endTimeText})
    public void showTimePickerDialog(View v) {
        if (v.getId() == R.id.startTimeText) {
            tracker.interactionTrack(getActivity().findViewById(R.id.startTimeText), tracker.getInteractionClicID());
            DialogFragment newFragment = TimePickerFragment.newInstance("start",mDay.getStart().getMinuteOfHour(),mDay.getStart().getHourOfDay());
            newFragment.show(getFragmentManager(), "startTimePicker");
        } else if (v.getId() == R.id.endTimeText) {
            tracker.interactionTrack(getActivity().findViewById(R.id.endTimeText), tracker.getInteractionClicID());
            DialogFragment newFragment = TimePickerFragment.newInstance("end",mDay.getEnd().getMinuteOfHour(),mDay.getEnd().getHourOfDay());
            newFragment.show(getFragmentManager(), "endTimePicker");
        }
    }

    @OnClick(R.id.dateText)
    public void showDatePickerDialog(View v) {
        tracker.interactionTrack(getActivity().findViewById(R.id.dateText), tracker.getInteractionClicID());
        DialogFragment newFragment = DatePickerFragment.newInstance(mDay.getDay().getDayOfMonth(),mDay.getDay().getMonthOfYear()-1,mDay.getDay().getYear());
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @OnClick(R.id.pauseDurationText)
    public void showDurationPickerDialog(View v) {
        tracker.interactionTrack(getActivity().findViewById(R.id.pauseDurationText), tracker.getInteractionClicID());
        DialogFragment newFragment = DurationPickerFragment.newInstance(mDay.getPause().getMillis());
        newFragment.show(getFragmentManager(), "durationPicker");
    }


    private void saveDayInfo()
    {
        try {
            if (mDay.isValid()) {
                mDay.computeTheExtraHours(mWorkProfile);
                mDayBox.put(mDay);
                EventBus.getDefault().post(new DayDatasetChangedEvent(TAG));
                String message = getResources().getString(R.string.save_day_message);
                Log.d(TAG, "Day: " + mDay.getDay().toString()+ " " + message);
                Toast.makeText(getActivity(), message,
                        Toast.LENGTH_LONG).show();
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

    @OnItemSelected(R.id.dayTypeSpinner)
    public void onDayTypePicked(AdapterView<ArrayAdapter<String>> parent, int position) {
        //This only works because the spinner and Day.DAY_TYPE values are in the same order
        mDay.setType(Day.DAY_TYPE.values()[position]);
        //updateDay();
        if(mDay.getType().compareTo(Day.DAY_TYPE.HOLIDAY) == 0 || mDay.getType().compareTo(Day.DAY_TYPE.DAY_OFF_IN_LIEU) == 0 ||
                mDay.getType().compareTo(Day.DAY_TYPE.OTHER) == 0 || mDay.getType().compareTo(Day.DAY_TYPE.ILLNESS) == 0){
            mStart.setEnabled(false);
            mEnd.setEnabled(false);
            mPause.setEnabled(false);
            mDay.isValid();
        }
        else{
            mStart.setEnabled(true);
            mEnd.setEnabled(true);
            mPause.setEnabled(true);
            if(mDay.getStart().compareTo(mDay.getDay().withTimeAtStartOfDay()) == 0 && mDay.getEnd().compareTo(mDay.getDay().withTimeAtStartOfDay()) == 0 &&
                    mDay.getPause().compareTo(Duration.millis(0)) == 0){
                mDay.setStart(new DateTime(0, 1, 1, 9, 0));
                mDay.setEnd(new DateTime(0, 1, 1, 17, 0));
                mDay.setPause(Duration.standardMinutes(45));
            }

        }

        EventBus.getDefault().post(new DayDatasetChangedEvent(TAG));
        tracker.interactionTrack(getActivity().findViewById(R.id.dayTypeSpinner), tracker.getInteractionClicID());
    }

    @Subscribe
    public void onTimePicked(TimePickedEvent event) {
        DateTime time = new DateTime(0, 1, 1, event.hoursOfDay, event.minute);

        switch (event.type) {
            case "start":
                mDay.setStart(time);
                tracker.interactionTrack(getActivity().findViewById(R.id.startTimeText), tracker.getInteractionEventID());
                break;
            case "end":
                mDay.setEnd(time);
                tracker.interactionTrack(getActivity().findViewById(R.id.endTimeText), tracker.getInteractionEventID());
                break;
        }
        mDay.computeTheExtraHours(mWorkProfile);
        EventBus.getDefault().post(new DayDatasetChangedEvent(TAG));
    }

    @Subscribe
    public void onDatePicked(DatePickedEvent event) {
        Day newDay = new Day(Day.DAY_TYPE.WORKDAY, new DateTime(event.year, event.month + 1, event.day, 0, 0), mDay.getStart(), mDay.getEnd(), mDay.getPause());
        Query<Day> newQuery = mDayBox.query().equal(Day_.id, newDay.getId()).build();
        Day dayFromData = newQuery.findUnique();
        if (dayFromData != null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.day_already_exists),
                    Toast.LENGTH_LONG).show();
        } else {
            mDayQuery = newQuery;
            newDay.setToDefaultDay();
            mDay = newDay;
            getTheCurrentWorkingProfile();
            EventBus.getDefault().post(new DayDatasetChangedEvent(TAG));
            //updateDay();
        }
    }

    @Subscribe
    public void onDurationPicked(DurationPickedEvent event) {
        mDay.setPause(Duration.millis(event.duration));
        EventBus.getDefault().post(new DayDatasetChangedEvent(TAG));
        //updateDay();
    }

    @Subscribe
    public void onDayDatasetChanged(DayDatasetChangedEvent event) {
        updateUI();
    }

    public void updateUI() {
        setDateText(false);
        setStartText(false);
        setEndText(false);
        setPauseText(false);
        setDayOvertime(false);
        setTotalOvertime(false);
        setTotalWorkingTime(false);
        setSpinnerItem();

    }

    private void updateOnError(int error) {
        setDateText(false);
        setStartText(false);
        setEndText(false);
        setPauseText(false);

        switch (error) {
            case R.string.no_work_on_weekend:
                setDateText(true);
                setPauseText(true);
                setStartText(true);
                setEndText(true);
                setPauseText(true);
                break;
            case R.string.pause_validation_short:
            case R.string.pause_validation_long:
                setPauseText(true);
                setStartText(true);
                setEndText(true);
                setPauseText(true);
                break;
            case R.string.too_many_hours:
                setTotalWorkingTime(true);
                break;
            case R.string.negative_total_time:
                setStartText(true);
                setEndText(true);
                setPauseText(true);
                setTotalWorkingTime(true);
                break;
            case R.string.outside_worktime:
                setStartText(true);
                setEndText(true);
                break;
        }
    }

    public Day getDay() {
        return mDay;
    }

    public void setDateText(boolean error) {
        String dateAt = (mDay.getDay().dayOfMonth().getAsShortText() + " " + mDay.getDay().monthOfYear().getAsShortText());
        String dayAt = (mDay.getDay().dayOfWeek().getAsShortText());
        mDate.setText(dayAt + " " + dateAt);
        setTextColor(mDate, error);
    }

    public void setStartText(boolean error) {
        String checkinAt = mDay.getStart().toString(Day.TIME_FORMATTER);
        mStart.setText(checkinAt);
        setTextColor(mStart, error);
    }

    public void setEndText(boolean error) {
        String checkoutAt = mDay.getEnd().toString(Day.TIME_FORMATTER);
        mEnd.setText(checkoutAt);
        setTextColor(mEnd, error);
    }

    public void setPauseText(boolean error) {
        String pauseAt = mDay.getPause().toPeriod().toString(Day.PERIOD_FORMATTER);
        mPause.setText(pauseAt);
        setTextColor(mPause, error);
    }

    private void setTotalWorkingTime(boolean error) {
        String totalWorkingTime = TimelyHelper.negativeTimePeriodFormatter(mDay.getTotalWorkingTime().toPeriod(), Day.PERIOD_FORMATTER);
        mtotalWorkingHours.setText(totalWorkingTime);
        setTextColor(mtotalWorkingHours, error);
    }

    private void setDayOvertime(boolean error) {
        mDay.computeTheExtraHours(mWorkProfile);
        String dayOvertime = TimelyHelper.negativeTimePeriodFormatter(mDay.getExtraHours().toPeriod(), Day.PERIOD_FORMATTER);
        mDayOvertime.setText(dayOvertime);
        setTextColor(mDayOvertime, error);
    }

    private void setTotalOvertime(boolean error) {
        String totalOvertime = TimelyHelper.negativeTimePeriodFormatter(Duration.millis(TimelyHelper.getTotalOvertimeForDay(mDay, mWorkProfile, mDayBox)).toPeriod(), Day.PERIOD_FORMATTER);
        mTotalOvertime.setText(totalOvertime);
        setTextColor(mTotalOvertime, error);
    }

    public void setSpinnerItem() {
        //This only works because the spinner and Day.DAY_TYPE values are in the same order
        mSpinner.setSelection(mDay.getType().id);
    }

    private void setTextColor(TextView view, boolean error) {
        if (error) {
            view.setTextColor(Color.RED);
        } else {
            view.setTextColor(Color.BLACK);
        }
    }
    @OnClick({R.id.dateImageView, R.id.dayTypeImageView,R.id.startTimeImageView, R.id.pauseDurationImageView,R.id.endTimeImageView,
            R.id.workingHoursimageView,R.id.dayOvertimetextViewNE,R.id.totalOvertimeTextViewNE,R.id.workingHourstext,
            R.id.dayOvertime,R.id.totalOvertime})
    public void clickUnEditableLabelsImages(View v) {
        int mSelectedText = v.getId();
        if (mSelectedText == R.id.dateImageView)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.dateImageView), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.dayTypeImageView)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.dayTypeImageView), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.startTimeImageView)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.startTimeImageView), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.pauseDurationImageView)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.pauseDurationImageView), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.endTimeImageView)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.endTimeImageView), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.workingHoursimageView)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.workingHoursimageView), tracker.getInteractionClicID());
        }
        else if ( mSelectedText == R.id.dayOvertimetextViewNE)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.dayOvertimetextViewNE), tracker.getInteractionClicID());
        }
        else if ( mSelectedText == R.id.totalOvertimeTextViewNE)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.totalOvertimeTextViewNE), tracker.getInteractionClicID());
        }
        else if ( mSelectedText == R.id.workingHourstext)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.workingHourstext), tracker.getInteractionClicID());
        }
        else if ( mSelectedText == R.id.dayOvertime)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.dayOvertime), tracker.getInteractionClicID());
        }
        else if ( mSelectedText == R.id.totalOvertime)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.totalOvertime), tracker.getInteractionClicID());
        }
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
                    tracker.interactionTrack(getActivity().findViewById(R.id.save), tracker.getInteractionClicID());
                    saveDayInfo();
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
