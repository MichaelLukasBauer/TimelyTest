package de.opti4apps.timelytest.data;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Minutes;

import de.opti4apps.timelytest.R;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Generated;
import io.objectbox.annotation.Keep;

//import static de.opti4apps.timelytest.data.Day_.day;

/**
 * Created by TCHATCHO on 26.04.2017.
 */

@Entity
public class WorkProfile {


    @Id(assignable = true)
    private long id;

    //@Id(assignable = true)
    private long userID;

    @Convert(converter = Day.DateTimeConverter.class, dbType = Long.class)
    private DateTime startDate;

    @Convert(converter = Day.DateTimeConverter.class, dbType = Long.class)
    private DateTime endDate;

    @Convert(converter = Day.DurationConverter.class, dbType = Long.class)
    private Duration monWorkHours;

    @Convert(converter = Day.DurationConverter.class, dbType = Long.class)
    private Duration tuesWorkHours;

    @Convert(converter = Day.DurationConverter.class, dbType = Long.class)
    private Duration wedWorkHours;

    @Convert(converter = Day.DurationConverter.class, dbType = Long.class)
    private Duration thursWorkHours;

    @Convert(converter = Day.DurationConverter.class, dbType = Long.class)
    private Duration friWorkHours;

    @Convert(converter = Day.DurationConverter.class, dbType = Long.class)
    private Duration previousOvertime;

    @Keep
    public WorkProfile(WorkProfile wp) {
        this.userID = wp.getUserID();
        this.monWorkHours = wp.getMonWorkHours();
        this.tuesWorkHours = wp.getTuesWorkHours();
        this.wedWorkHours = wp.getWedWorkHours();
        this.thursWorkHours = wp.getThursWorkHours();
        this.friWorkHours = wp.getFriWorkHours();
        this.id = wp.getStartDate().withTimeAtStartOfDay().getMillis();
        this.startDate = wp.getStartDate();
        this.endDate = wp.getEndDate();
        this.previousOvertime = wp.getPreviousOvertime();
    }
    @Keep
    public WorkProfile(long userID, Duration monWorkHours,
                       Duration tuesWorkHours, Duration wedWorkHours, Duration thursWorkHours,
                       Duration friWorkHours, Duration previousOvertime) {
        this.userID = userID;
        this.monWorkHours = monWorkHours;
        this.tuesWorkHours = tuesWorkHours;
        this.wedWorkHours = wedWorkHours;
        this.thursWorkHours = thursWorkHours;
        this.friWorkHours = friWorkHours;
        DateTime day = new DateTime().dayOfMonth().withMinimumValue();
        this.id = day.withTimeAtStartOfDay().getMillis();
        this.startDate = day;
        this.endDate = day.plusMonths(1).minusDays(1);
        this.previousOvertime = previousOvertime;
    }
    @Generated(hash = 1185460489)
    public WorkProfile(long id, long userID, DateTime startDate, DateTime endDate, Duration monWorkHours, Duration tuesWorkHours, Duration wedWorkHours, Duration thursWorkHours,
            Duration friWorkHours, Duration previousOvertime) {
        this.id = id;
        this.userID = userID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monWorkHours = monWorkHours;
        this.tuesWorkHours = tuesWorkHours;
        this.wedWorkHours = wedWorkHours;
        this.thursWorkHours = thursWorkHours;
        this.friWorkHours = friWorkHours;
        this.previousOvertime = previousOvertime;
    }
    @Generated(hash = 1509302824)
    public WorkProfile() {
    }

    public void updateWorkingProfile(WorkProfile wp){
        this.monWorkHours = wp.getMonWorkHours();
        this.tuesWorkHours = wp.getTuesWorkHours();
        this.wedWorkHours = wp.getWedWorkHours();
        this.thursWorkHours = wp.getThursWorkHours();
        this.friWorkHours = wp.getFriWorkHours();
        this.previousOvertime = wp.getPreviousOvertime();
    }

    public boolean isWorkingHoursEquals(WorkProfile wp)
    {
        return (this.monWorkHours == wp.monWorkHours) && (this.tuesWorkHours == wp.tuesWorkHours) &&
                (this.friWorkHours == wp.friWorkHours) && (this.thursWorkHours == wp.thursWorkHours) &&
                (this.wedWorkHours == wp.wedWorkHours);
    }
      public boolean isValid() throws IllegalArgumentException {
                if (monWorkHours.getMillis()  > 36000000 || tuesWorkHours.getMillis()  > 36000000 ||
                        wedWorkHours.getMillis()  > 36000000 || thursWorkHours.getMillis()  > 36000000
                        || friWorkHours.getMillis()  > 36000000)  {
                    throw new IllegalArgumentException(String.valueOf(R.string.too_many_hours));
                }else if (getTotalWorkingTime().getMillis()  > 144000000) {
                    throw new IllegalArgumentException(String.valueOf(R.string.week_exceed_total_time));
                }else if (getTotalWorkingTime().getMillis() == 0) {
                    throw new IllegalArgumentException(String.valueOf(R.string.no_weekly_working_hours));
                }
        return true;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Duration getMonWorkHours() {
        return monWorkHours;
    }

    public void setMonWorkHours(Duration monWorkHours) {
        this.monWorkHours = monWorkHours;
    }

    public Duration getTuesWorkHours() {
        return tuesWorkHours;
    }

    public void setTuesWorkHours(Duration tuesWorkHours) {
        this.tuesWorkHours = tuesWorkHours;
    }

    public Duration getWedWorkHours() {
        return wedWorkHours;
    }

    public void setWedWorkHours(Duration wedWorkHours) {
        this.wedWorkHours = wedWorkHours;
    }

    public Duration getThursWorkHours() {
        return thursWorkHours;
    }

    public void setThursWorkHours(Duration thursWorkHours) {
        this.thursWorkHours = thursWorkHours;
    }

    public Duration getFriWorkHours() {
        return friWorkHours;
    }

    public void setFriWorkHours(Duration friWorkHours) {
        this.friWorkHours = friWorkHours;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public DateTime getStartDate() { return startDate; }

    public void setStartDate(DateTime startDate) { this.startDate = startDate; }

    public DateTime getEndDate() { return endDate; }

    public void setEndDate(DateTime endDate) { this.endDate = endDate; }

    public Duration getTotalWorkingTime() {
        return Duration.millis(monWorkHours.getMillis()).plus(tuesWorkHours.getMillis()).plus(wedWorkHours.getMillis()).plus(thursWorkHours.getMillis()).plus(friWorkHours.getMillis());
    }

    public Duration getPreviousOvertime() {
        return previousOvertime;
    }

    public void setPreviousOvertime(Duration previousOvertime) {
        this.previousOvertime = previousOvertime;
    }

}
