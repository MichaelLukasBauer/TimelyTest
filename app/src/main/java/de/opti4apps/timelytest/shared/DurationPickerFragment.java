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

    private String day;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long duration = 0;
        return new TimeDurationPickerDialog(getActivity(), this, duration, TimeDurationPicker.HH_MM);
    }

    public void setDay(String day) {
        this.day = day;
    }

    @Override
    public void onDurationSet(TimeDurationPicker view, long duration) {
        EventBus.getDefault().post(new DurationPickedEvent(duration,day));
    }
}
