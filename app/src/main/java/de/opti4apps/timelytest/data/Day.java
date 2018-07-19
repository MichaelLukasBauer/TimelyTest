package de.opti4apps.timelytest.data;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Calendar;

import de.opti4apps.timelytest.R;
import de.opti4apps.timelytest.shared.TrackerHelper;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Generated;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Keep;
import io.objectbox.converter.PropertyConverter;

/**
 * Created by Miluba on 30.03.2017.
 */
@Entity
public class Day {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm");
    public static final PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
            .appendHours()
            .appendSuffix("h ")
            .appendMinutes()
            .appendSuffix("min")
            .toFormatter();
    @Id(assignable = true)
    private long id;
    private long userID;
    @Convert(converter = DayTypeConverter.class, dbType = Integer.class)
    private DAY_TYPE type;
    @Index
    @Convert(converter = DateTimeConverter.class, dbType = Long.class)
    private DateTime day;
    @Convert(converter = DateTimeConverter.class, dbType = Long.class)
    private DateTime start;
    @Convert(converter = DateTimeConverter.class, dbType = Long.class)
    private DateTime end;
    @Convert(converter = DurationConverter.class, dbType = Long.class)
    private Duration pause;
    @Convert(converter = DurationConverter.class, dbType = Long.class)
    private Duration extraHours;
    @Keep
    public Day(long userID,DAY_TYPE type, DateTime day, DateTime start, DateTime end, Duration pause) {
        this.userID = userID;
        this.type = type;
        this.day = day;
        this.start = start;
        this.end = end;
        this.pause = pause;
        this.extraHours = Duration.ZERO;
        this.id = day.withTimeAtStartOfDay().getMillis() + userID;
    }

    @Generated(hash = 278087938)
    public Day(long id, long userID, DAY_TYPE type, DateTime day, DateTime start, DateTime end, Duration pause, Duration extraHours) {
        this.id = id;
        this.userID = userID;
        this.type = type;
        this.day = day;
        this.start = start;
        this.end = end;
        this.pause = pause;
        this.extraHours = extraHours;
    }

    @Generated(hash = 866989762)
    public Day() {
    }

    public boolean isValid() throws IllegalArgumentException {
        String dayText = this.day.dayOfWeek().getAsText();
        if(convertDayTextToNumber(dayText) ==  Calendar.SATURDAY || convertDayTextToNumber(dayText) ==  Calendar.SUNDAY  )
        {
            throw new IllegalArgumentException(String.valueOf(R.string.no_work_on_weekend));
        }

        switch (type) {
            case WORKDAY:
            case BUSINESS_TRIP:
            case FURTHER_EDUCATION:
            case DOCTOR_APPOINTMENT:
                if (start.getHourOfDay() < 7 || (end.getHourOfDay() > 19 && end.getMinuteOfHour() > 30)) {
                    throw new IllegalArgumentException(String.valueOf(R.string.outside_worktime));
                }
                if (end.getMillis() - start.getMillis() - pause.getMillis() < 0) {
                    throw new IllegalArgumentException(String.valueOf(R.string.negative_total_time));
                }
                if (end.getMillis() - start.getMillis() - pause.getMillis() > Hours.hours(10).toStandardDuration().getMillis()) {
                    throw new IllegalArgumentException(String.valueOf(R.string.too_many_hours));
                }
                if (end.getMillis() - start.getMillis() >= Hours.hours(6).toStandardDuration().getMillis() && pause.getMillis() < Minutes.minutes(30).toStandardDuration().getMillis()) {
                    throw new IllegalArgumentException(String.valueOf(R.string.pause_validation_short));
                }
                if (end.getMillis() - start.getMillis() >= Hours.hours(9).toStandardDuration().getMillis() && pause.getMillis() < Minutes.minutes(45).toStandardDuration().getMillis()) {
                    throw new IllegalArgumentException(String.valueOf(R.string.pause_validation_long));
                }
                break;
            case HOLIDAY:
            case DAY_OFF_IN_LIEU:
            case ILLNESS:
            case OTHER:
                start = day.withTimeAtStartOfDay();
                end = day.withTimeAtStartOfDay();
                pause = Duration.millis(0);
                break;
        }
        return true;
    }

    public Duration getTotalWorkingTime() {
        return Duration.millis(end.toInstant().minus(start.toInstant().getMillis()).minus(pause.getMillis()).getMillis());
    }

    public long getId() {
        return id;
    }

    public long getUserID() {
        return userID;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DAY_TYPE getType() {
        return type;
    }

    public void setType(DAY_TYPE type) {
        this.type = type;
    }

    public DateTime getDay() {
        return day;
    }

    public Calendar getDayAsCalendar(){
        Calendar dayCal = Calendar.getInstance();
        dayCal.setTime(day.toDate());
        return dayCal;
    }

    public void setDay(DateTime day) {
        this.day = day;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public Duration getPause() {
        return pause;
    }

    public void setPause(Duration pause) {
        this.pause = pause;
    }

    public Duration getExtraHours() { return extraHours; }

    public void setExtraHours(Duration extraHours) { this.extraHours = extraHours; }

    public  long convertDayTextToNumber (String dafOfWeek)
    {
        long dayAsNumber = 0;
        if(dafOfWeek.compareToIgnoreCase("monday") == 0)
        {
            dayAsNumber = Calendar.MONDAY;
        }
        else if(dafOfWeek.compareToIgnoreCase("tuesday") == 0)
        {
            dayAsNumber = Calendar.TUESDAY;
        }
        else if(dafOfWeek.compareToIgnoreCase("wednesday") == 0)
        {
            dayAsNumber = Calendar.WEDNESDAY;
        }
        else if(dafOfWeek.compareToIgnoreCase("thursday") == 0)
        {
            dayAsNumber = Calendar.THURSDAY;
        }
        else if(dafOfWeek.compareToIgnoreCase("friday") == 0)
        {
            dayAsNumber = Calendar.FRIDAY;
        }
        else if(dafOfWeek.compareToIgnoreCase("saturday") == 0)
        {
            dayAsNumber = Calendar.SATURDAY;
        }
        else if(dafOfWeek.compareToIgnoreCase("sunday") == 0)
        {
            dayAsNumber = Calendar.SUNDAY;
        }

        return dayAsNumber;

    }

    public void setToDefaultDay()
    {
        this.type = Day.DAY_TYPE.WORKDAY;
       // this.start = new DateTime(getDay()).withTime(9,0,0,0);
          this.start = new DateTime(0, 1, 1, 9, 0);
       // this.end = new DateTime(getDay()).withTime(17,0,0,0);
          this.end = new DateTime(0, 1, 1, 17, 0);
        this.pause = Duration.standardMinutes(45);
    }
    public void computeTheExtraHours(WorkProfile wp)
    {
        if (convertDayTextToNumber(this.day.dayOfWeek().getAsText()) !=  Calendar.SATURDAY && convertDayTextToNumber(this.day.dayOfWeek().getAsText()) !=  Calendar.SUNDAY &&
                (this.getType() == DAY_TYPE.WORKDAY || this.getType() == DAY_TYPE.BUSINESS_TRIP || this.getType() == DAY_TYPE.FURTHER_EDUCATION || this.getType() == DAY_TYPE.DOCTOR_APPOINTMENT)) {

            if (convertDayTextToNumber(this.day.dayOfWeek().getAsText()) == Calendar.MONDAY) {
                this.extraHours = getTotalWorkingTime().minus(wp.getMonWorkHours());
            } else if (convertDayTextToNumber(this.day.dayOfWeek().getAsText()) == Calendar.TUESDAY) {
                setExtraHours(getTotalWorkingTime().minus(wp.getTuesWorkHours()));
            } else if (convertDayTextToNumber(this.day.dayOfWeek().getAsText()) == Calendar.WEDNESDAY) {
                this.extraHours = getTotalWorkingTime().minus(wp.getWedWorkHours());
            } else if (convertDayTextToNumber(this.day.dayOfWeek().getAsText()) == Calendar.THURSDAY) {
                this.extraHours = getTotalWorkingTime().minus(wp.getThursWorkHours());
            } else if (convertDayTextToNumber(this.day.dayOfWeek().getAsText()) == Calendar.FRIDAY) {
                this.extraHours = getTotalWorkingTime().minus(wp.getFriWorkHours());
            }
        }
        else
        {
            this.extraHours = Duration.ZERO;
        }
    }

    public Duration getDayWorkingHoursFromWP(WorkProfile wp){
        if (convertDayTextToNumber(this.day.dayOfWeek().getAsText()) == Calendar.MONDAY) {
            return wp.getMonWorkHours();
        } else if (convertDayTextToNumber(this.day.dayOfWeek().getAsText()) == Calendar.TUESDAY) {
            return wp.getTuesWorkHours();
        } else if (convertDayTextToNumber(this.day.dayOfWeek().getAsText()) == Calendar.WEDNESDAY) {
            return wp.getWedWorkHours();
        } else if (convertDayTextToNumber(this.day.dayOfWeek().getAsText()) == Calendar.THURSDAY) {
            return wp.getThursWorkHours();
        } else if (convertDayTextToNumber(this.day.dayOfWeek().getAsText()) == Calendar.FRIDAY) {
            return wp.getFriWorkHours();
        }
        else
            return Duration.standardMinutes(0);
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public enum DAY_TYPE {
       WORKDAY(0), BUSINESS_TRIP(1), HOLIDAY(2), ILLNESS(3), DAY_OFF_IN_LIEU(4), FURTHER_EDUCATION(5), DOCTOR_APPOINTMENT(6),  OTHER(7);

        public final int id;

        DAY_TYPE(int id) {
            this.id = id;
        }


    }

    public static class DayTypeConverter implements PropertyConverter<DAY_TYPE, Integer> {

        @Override
        public DAY_TYPE convertToEntityProperty(Integer integer) {

            for (DAY_TYPE type : DAY_TYPE.values()) {
                if (type.id == integer) {
                    return type;
                }
            }
            return DAY_TYPE.WORKDAY;
        }

        @Override
        public Integer convertToDatabaseValue(DAY_TYPE day_type) {
            return day_type == null ? null : day_type.id;
        }
    }

    public static class DateTimeConverter implements PropertyConverter<DateTime, Long> {

        @Override
        public DateTime convertToEntityProperty(Long aLong) {
            return new DateTime(aLong);
        }

        @Override
        public Long convertToDatabaseValue(DateTime dateTime) {
            return dateTime.getMillis();
        }
    }

    public static class DurationConverter implements PropertyConverter<Duration, Long> {

        @Override
        public Duration convertToEntityProperty(Long aLong) {
            return Duration.millis(aLong);
        }

        @Override
        public Long convertToDatabaseValue(Duration duration) {
            return duration.getMillis();
        }
    }

}
