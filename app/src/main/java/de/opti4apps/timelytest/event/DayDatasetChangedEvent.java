package de.opti4apps.timelytest.event;

/**
 * Created by Miluba on 04.04.2017.
 */

public class DayDatasetChangedEvent {
    private final String tag;

    public DayDatasetChangedEvent(String tag) {
        this.tag = tag;
    }
}
