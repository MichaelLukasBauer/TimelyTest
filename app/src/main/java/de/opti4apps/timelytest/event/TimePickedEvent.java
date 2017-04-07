package de.opti4apps.timelytest.event;

/**
 * Created by Miluba on 03.04.2017.
 */

public class TimePickedEvent {

    public final int hoursOfDay;
    public final int minute;
    public final String type;

    public TimePickedEvent(int hoursOfDay, int minute, String type) {
        this.hoursOfDay = hoursOfDay;
        this.minute = minute;
        this.type = type;

    }


}
