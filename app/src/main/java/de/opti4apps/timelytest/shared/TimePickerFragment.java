package de.opti4apps.timelytest.shared;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import de.opti4apps.timelytest.event.TimePickedEvent;

/**
 * Created by Miluba on 03.04.2017.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private static final String ARG_TYPE = "type";
    private static final String ARG_MINUTE = "minute";
    private static final String ARG_HOUR_OF_DAY = "hour";
    private String type;
    private int hour = 0;
    private int minute = 0;

    public static TimePickerFragment newInstance(String type) {
        TimePickerFragment fragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public static TimePickerFragment newInstance(String type, int minute, int hourOfDay) {
        TimePickerFragment fragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putInt(ARG_MINUTE, minute);
        args.putInt(ARG_HOUR_OF_DAY, hourOfDay);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
            hour = getArguments().getInt(ARG_HOUR_OF_DAY);
            minute = getArguments().getInt(ARG_MINUTE);

        }
        final Calendar c = Calendar.getInstance();
        // Use the current time as the default values for the picker
        if (hour == 0 && minute == 0) {
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        EventBus.getDefault().post(new TimePickedEvent(hourOfDay, minute, type));
    }

}
