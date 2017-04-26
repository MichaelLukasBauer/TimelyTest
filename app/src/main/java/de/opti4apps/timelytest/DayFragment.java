package de.opti4apps.timelytest;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.Day_;
import de.opti4apps.timelytest.event.DatePickedEvent;
import de.opti4apps.timelytest.event.DayDatasetChangedEvent;
import de.opti4apps.timelytest.event.DurationPickedEvent;
import de.opti4apps.timelytest.event.TimePickedEvent;
import de.opti4apps.timelytest.shared.DatePickerFragment;
import de.opti4apps.timelytest.shared.DurationPickerFragment;
import de.opti4apps.timelytest.shared.TimePickerFragment;
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

    @BindView(R.id.dateText)
    TextView mDate;

    @BindView(R.id.startTimeText)
    TextView mStart;

    @BindView(R.id.endTimeText)
    TextView mEnd;

    @BindView(R.id.pauseDurationText)
    TextView mPause;

    @BindView(R.id.dayTypeSpinner)
    Spinner mSpinner;

    private Day mDay;
    private Box<Day> mDayBox;
    private Query<Day> mDayQuery;


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
    public static DayFragment newInstance(long dayID) {
        DayFragment fragment = new DayFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DAY_ID, dayID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDayBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Day.class);
        if (getArguments() != null) {
            long dayID = getArguments().getLong(ARG_DAY_ID);

            if (dayID == 0) {
                Day day = new Day(Day.DAY_TYPE.WORKDAY, DateTime.now(), DateTime.now(), DateTime.now(), Duration.standardMinutes(0));
                dayID = day.getId();

                mDayBox.put(day);
            }
            mDayQuery = mDayBox.query().equal(Day_.id, dayID).build();
            mDay = mDayQuery.findUnique();

        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_day, container, false);
        ButterKnife.bind(this, view);


        final String[] dayTypes = getResources().getStringArray(R.array.day_type_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, dayTypes);
        mSpinner.setAdapter(adapter);

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

    @OnClick({R.id.startTimeText, R.id.endTimeText})
    public void showTimePickerDialog(View v) {
        if (v.getId() == R.id.startTimeText) {

            DialogFragment newFragment = TimePickerFragment.newInstance("start");
            newFragment.show(getFragmentManager(), "startTimePicker");
        } else if (v.getId() == R.id.endTimeText) {
            DialogFragment newFragment = TimePickerFragment.newInstance("end");
            newFragment.show(getFragmentManager(), "endTimePicker");
        }
    }

    @OnClick(R.id.dateText)
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @OnClick(R.id.pauseDurationText)
    public void showDurationPickerDialog(View v) {
        DialogFragment newFragment = new DurationPickerFragment();
        newFragment.show(getFragmentManager(), "durationPicker");
    }

    @OnItemSelected(R.id.dayTypeSpinner)
    public void onDayTypePicked(AdapterView<ArrayAdapter<String>> parent, int position) {
        //This only works because the spinner and Day.DAY_TYPE values are in the same order
        mDay.setType(Day.DAY_TYPE.values()[position]);
        updateDay();
    }

    @Subscribe
    public void onTimePicked(TimePickedEvent event) {
        DateTime time = new DateTime(0, 1, 1, event.hoursOfDay, event.minute);
        switch (event.type) {
            case "start":
                mDay.setStart(time);
                break;
            case "end":
                mDay.setEnd(time);
                break;
        }
        updateDay();
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
            mDay = newDay;
            updateDay();
        }
    }

    @Subscribe
    public void onDurationPicked(DurationPickedEvent event) {
        mDay.setPause(Duration.millis(event.duration));
        updateDay();
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
        setSpinnerItem();

    }

    private void updateOnError(int error) {
        setDateText(false);
        setStartText(false);
        setEndText(false);
        setPauseText(false);

        switch (error) {
            case R.string.pause_validation_short:
            case R.string.pause_validation_long:
                setPauseText(true);
                break;
            case R.string.too_many_hours:
            case R.string.negative_total_time:
                setStartText(true);
                setEndText(true);
                setPauseText(true);
                break;
            case R.string.outside_worktime:
                setStartText(true);
                setEndText(true);
                break;
        }
    }

    private void updateDay() {
        try {
            if (mDay.isValid()) {
                mDayBox.put(mDay);
                EventBus.getDefault().post(new DayDatasetChangedEvent(TAG));
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


}
