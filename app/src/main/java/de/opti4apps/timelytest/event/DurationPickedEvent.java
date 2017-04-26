package de.opti4apps.timelytest.event;

/**
 * Created by Miluba on 03.04.2017.
 */

public class DurationPickedEvent {
    public final long duration;

    public DurationPickedEvent(long duration) {
        this.duration = duration;
    }
}
