package de.opti4apps.timelytest.data;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import de.opti4apps.timelytest.R;
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

    public enum DAY_TYPE {
        OTHER(0), WORKDAY(1), BUSINESS_TRIP(2), HOLIDAY(3), DOCTOR_APPOINTMENT(4), DAY_OFF_IN_LIEU(5), FURTHER_EDUCATION(6);

        public final int id;

         DAY_TYPE(int id) {
            this.id = id;
        }


    }

    @Id(assignable = true)
    private long id;

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

    @Keep
    public Day(DAY_TYPE type, DateTime day, DateTime start, DateTime end, Duration pause) {
        this.type = type;
        this.day = day;
        this.start = start;
        this.end = end;
        this.pause = pause;
        this.id = day.withTimeAtStartOfDay().getMillis();
    }

    @Generated(hash = 439792684)
    public Day(long id, DAY_TYPE type, DateTime day, DateTime start, DateTime end, Duration pause) {
        this.id = id;
        this.type = type;
        this.day = day;
        this.start = start;
        this.end = end;
        this.pause = pause;
    }


    @Generated(hash = 866989762)
    public Day() {
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

    public boolean isValid() throws IllegalArgumentException {
        switch (type) {
            case WORKDAY:
            case BUSINESS_TRIP:
            case FURTHER_EDUCATION:
            case DOCTOR_APPOINTMENT:
                if (start.getHourOfDay() < 7 || (end.getHourOfDay() > 19 && end.getMinuteOfHour() > 30)) {
                    throw new IllegalArgumentException(String.valueOf(R.string.outside_worktime));
                }
                if (end.getMillis() - start.getMillis() - pause.getMillis()<= 0) {
                    throw new IllegalArgumentException(String.valueOf(R.string.negative_total_time));
                }
                if (end.getMillis() - start.getMillis() - pause.getMillis() > Hours.hours(10).toStandardDuration().getMillis()) {
                    throw new IllegalArgumentException(String.valueOf(R.string.too_many_hours));
                }
                if (end.getMillis() - start.getMillis() >= Hours.hours(6).toStandardDuration().getMillis() && pause.getMillis() < Minutes.minutes(30).toStandardDuration().getMillis()) {
                    throw new IllegalArgumentException(String.valueOf(R.string.pause_validation_short));
                }
                if (end.getMillis() - start.getMillis() >= Hours.hours(8).toStandardDuration().getMillis() && pause.getMillis() < Minutes.minutes(45).toStandardDuration().getMillis()) {
                    throw new IllegalArgumentException(String.valueOf(R.string.pause_validation_long));
                }
                break;
            case HOLIDAY:
            case DAY_OFF_IN_LIEU:
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


    public DAY_TYPE getType() {
        return type;
    }


    public void setType(DAY_TYPE type) {
        this.type = type;
    }


    public DateTime getDay() {
        return day;
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

    public void setId(long id) {
        this.id = id;
    }

}
