package de.opti4apps.timelytest.event;

/**
 * Created by Miluba on 03.04.2017.
 */

public class DaySelectedEvent {

    public final long dayID;

    public DaySelectedEvent(long dayID) {
        this.dayID = dayID;
    }
}
