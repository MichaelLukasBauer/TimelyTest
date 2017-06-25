package de.opti4apps.timelytest.data;

import org.joda.time.Duration;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Keep;
import io.objectbox.annotation.Generated;

/**
 * Created by TCHATCHO on 07.06.2017.
 */

@Entity
public class TotalExtraHours {

    @Id(assignable = true)
    private long id;

    @Convert(converter = Day.DurationConverter.class, dbType = Long.class)
    private Duration totalExtraHours;

    @Keep
    public TotalExtraHours (long id, Duration totalExtraHours)
    {
        this.id = id;
        this.totalExtraHours = totalExtraHours;
    }

    @Generated(hash = 260384257)
    public TotalExtraHours() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Duration getTotalExtraHours() {
        return totalExtraHours;
    }

    public void setTotalExtraHours(Duration totalExtraHours) {
        this.totalExtraHours = totalExtraHours;
    }

    public void updateTotalExtraHours(Duration newExtraHours)
    {
        this.totalExtraHours = this.totalExtraHours.plus(newExtraHours);
    }
}
