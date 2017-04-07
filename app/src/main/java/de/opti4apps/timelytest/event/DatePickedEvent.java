package de.opti4apps.timelytest.event;

/**
 * Created by Miluba on 03.04.2017.
 */

public class DatePickedEvent {

    public final int year;
    public final int month;
    public final int day;

    public DatePickedEvent(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

}
