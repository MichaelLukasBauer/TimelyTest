package de.opti4apps.timelytest.shared;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.NumberPicker;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import de.opti4apps.timelytest.R;
import de.opti4apps.timelytest.event.DatePickedEvent;

/**
 * Created by Kateryna Sergieieva on 02.08.2017.
 */

public class MonthYearPickerFragment extends DialogFragment{
    private static final int MAX_YEAR = 2099;
    private static final int MIN_YEAR = 2000;

    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private DatePickerDialog.OnDateSetListener listener;

    private int year;
    private int month;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            year = getArguments().getInt(ARG_YEAR);
            month = getArguments().getInt(ARG_MONTH);
            //setRetainInstance(true);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Calendar cal = Calendar.getInstance();

        View dialog = inflater.inflate(R.layout.fragment_month_year_picker, null);
        final NumberPicker monthPicker = (NumberPicker) dialog.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = (NumberPicker) dialog.findViewById(R.id.picker_year);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(month);
        monthPicker.setDisplayedValues( new String[] { "January", "February", "March", "April", "May", "June","July", "August", "September","October", "November", "December" } );

        yearPicker.setMinValue(MIN_YEAR);
        yearPicker.setMaxValue(MAX_YEAR);
        yearPicker.setValue(year);

        builder.setView(dialog)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EventBus.getDefault().post(new DatePickedEvent(yearPicker.getValue(), monthPicker.getValue(), 1));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MonthYearPickerFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}
