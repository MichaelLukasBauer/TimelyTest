package de.opti4apps.timelytest.event;

/**
 * Created by Miluba on 03.04.2017.
 */

public class DurationPickedEvent {
    public final long duration;
    public final String day;

    public DurationPickedEvent(long duration) {
        this.duration = duration;
        this.day = "";
    }
    public DurationPickedEvent(long duration,String day) {
        this.duration = duration;
        this.day = day;
    }

}
