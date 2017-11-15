package de.opti4apps.timelytest.shared;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.Duration;

import java.util.Calendar;

import de.opti4apps.timelytest.App;
import de.opti4apps.timelytest.data.WorkProfile;
import de.opti4apps.timelytest.event.DatePickedEvent;
import io.objectbox.Box;

/**
 * Created by Miluba on 03.04.2017.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final String ARG_USER_ID = "userID";
    private static final String ARG_DAY = "day";
    private static final String ARG_MONTH = "month";
    private static final String ARG_YEAR = "year";
    private int  day;
    private int month = 0;
    private int year = 0;
    long userID;
    private Box<WorkProfile> mWorkProfileBox;

    public static DatePickerFragment newInstance(int day,int month, int year,long userID) {
        DatePickerFragment fragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DAY, day);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_YEAR, year);
        args.putLong(ARG_USER_ID, userID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments() != null) {
            day = getArguments().getInt(ARG_DAY);
            month = getArguments().getInt(ARG_MONTH);
            year = getArguments().getInt(ARG_YEAR);
            userID = getArguments().getLong(ARG_USER_ID);
        }
        else
        {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
            userID = 0;
        }

        mWorkProfileBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(WorkProfile.class);

        WorkProfile maxWorkProfile = TimelyHelper.getMaxWorkingProfile(mWorkProfileBox,userID);
        WorkProfile minWorkProfile = TimelyHelper.getMinWorkingProfile(mWorkProfileBox,userID);

//        maxWorkProfile = new WorkProfile(145236, 123, maxWorkProfile.getStartDate().plusMonths(1), maxWorkProfile.getEndDate().plusMonths(1),
//                Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0),
//                Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0));

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(maxWorkProfile.getEndDate().getMillis());
        datePickerDialog.getDatePicker().setMinDate(minWorkProfile.getStartDate().getMillis());

        // Create a new instance of DatePickerDialog and return it
        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        EventBus.getDefault().post(new DatePickedEvent(year, month, day));
    }
}
