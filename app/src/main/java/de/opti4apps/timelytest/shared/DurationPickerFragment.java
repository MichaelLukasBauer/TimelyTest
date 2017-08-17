package de.opti4apps.timelytest.shared;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.greenrobot.eventbus.EventBus;

import de.opti4apps.timelytest.event.DurationPickedEvent;
import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

/**
 * Created by Miluba on 03.04.2017.
 */

public class DurationPickerFragment extends DialogFragment implements TimeDurationPickerDialog.OnDurationSetListener {

    private static final String ARG_DURATION = "duration";
    private long mDuration = 0 ;

    public static  DurationPickerFragment newInstance(long duration)
    {
        DurationPickerFragment fragment = new DurationPickerFragment();

        // Supply duration input as an argument.
        Bundle args = new Bundle();
        args.putLong(ARG_DURATION, duration);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            mDuration = getArguments().getLong(ARG_DURATION);;

        }
        return new TimeDurationPickerDialog(getActivity(), this, mDuration, TimeDurationPicker.HH_MM);
    }

    @Override
    public void onDurationSet(TimeDurationPicker view, long duration) {
        EventBus.getDefault().post(new DurationPickedEvent(duration));
    }

}
