package de.opti4apps.timelytest.shared;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.Day_;
import de.opti4apps.timelytest.data.WorkProfile;
import de.opti4apps.timelytest.data.WorkProfile_;
import io.objectbox.Box;
import io.objectbox.query.Query;

/**
 * Created by TCHATCHO on 25.06.2017.
 */

public class TimelyHelper {

    private static Day mDay;
    private static Box<Day> mDayBox;
    private static WorkProfile mWorkProfile;
    private static Box<WorkProfile> mWorkProfileBox;
    private static Query<Day> mDayQuery;
    private static Query<WorkProfile> mWorkProfileQuery;

    public static WorkProfile getValidWorkingProfileByDay(Day d, Box<WorkProfile> mWorkProfileBox, Box<Day> mDayBox)
    {
        DateTime month = d.getDay().dayOfMonth().withMinimumValue();
        return getValidWorkingProfile(month, mWorkProfileBox, mDayBox);
    }

    public static WorkProfile getValidWorkingProfile(DateTime month, Box<WorkProfile> mWorkProfileBox, Box<Day> mDayBox)
    {
        WorkProfile resultWorkProfile = getWorkProfileByMonth(month, mWorkProfileBox);
        if(resultWorkProfile == null){
            WorkProfile lastMonthWorkProfile = getWorkProfileByMonth(month.minusMonths(1), mWorkProfileBox);
            if(lastMonthWorkProfile != null){
                resultWorkProfile = new WorkProfile(lastMonthWorkProfile);
                resultWorkProfile.setId(month.withTimeAtStartOfDay().getMillis());
                resultWorkProfile.setStartDate(month);
                resultWorkProfile.setEndDate(month.plusMonths(1).minusDays(1));
                resultWorkProfile.setPreviousOvertime(getMonthTotalOvertime(lastMonthWorkProfile, mDayBox));
                mWorkProfileBox.put(resultWorkProfile);
            }
        }
        return resultWorkProfile;
    }

    public static WorkProfile getWorkProfileByMonth(DateTime month, Box<WorkProfile> mWorkProfileBox){
        DateTime startMonth = month.dayOfMonth().withMinimumValue().withTime(0, 0, 0, 0);
        DateTime endMonth = month.plusMonths(1).minusDays(1).withTime(23, 59, 0, 0);
        mWorkProfileQuery = mWorkProfileBox.query().between(WorkProfile_.startDate, startMonth.toDate(), endMonth.toDate()).build();
        WorkProfile wp = mWorkProfileQuery.findUnique();
        return wp;
    }

    public static boolean isDayInWokingProfile(Day d, WorkProfile wp)
    {
        Boolean result = false;
        if ((d.getDay().withTimeAtStartOfDay().getMillis() >= wp.getStartDate().withTimeAtStartOfDay().getMillis()) && (d.getDay().withTimeAtStartOfDay().getMillis() <= wp.getEndDate().withTimeAtStartOfDay().getMillis()))
        {
            result = true;
        }


        return result;

    }

    public static long getTotalOvertimeForDay(Day mDay, WorkProfile wp, Box<Day> mDayBox)
    {
        long totalOvertime = 0;

        DateTime startMonth = wp.getStartDate().withTime(0, 0, 0, 0);
        DateTime tillCurrentDay =  mDay.getDay().minusDays(1).withTime(23, 59, 0, 0);

        mDayQuery = mDayBox.query().between(Day_.day, startMonth.toDate() , tillCurrentDay.toDate()).build();
        List<Day> allDay = mDayQuery.find();
        allDay.add(mDay);
        for (Day d: allDay)
        {
            if(d.getType().compareTo(Day.DAY_TYPE.DAY_OFF_IN_LIEU) == 0){
                Duration workingHoursfromWP = d.getDayWorkingHoursFromWP(wp);
                totalOvertime -= workingHoursfromWP.getMillis();
            }
            else {
                d.computeTheExtraHours(wp);
                totalOvertime += d.getExtraHours().getMillis();
            }
        }
        totalOvertime += wp.getPreviousOvertime().getMillis();
        return totalOvertime;
    }

    public static Duration getMonthTotalOvertime(WorkProfile wp, Box<Day> mDayBox){
        long totalOvertime = 0;

        DateTime startMonth = wp.getStartDate().withTime(0, 0, 0, 0);
        DateTime endMonth =  wp.getEndDate().minusDays(1).withTime(23, 59, 0, 0);

        mDayQuery = mDayBox.query().between(Day_.day, startMonth.toDate() , endMonth.toDate()).orderDesc(Day_.day).build();

        Day lastDayOfMonth = mDayQuery.findFirst();

        totalOvertime = getTotalOvertimeForDay(lastDayOfMonth, wp, mDayBox);
        return Duration.millis(totalOvertime);
    }

    public static String negativeTimePeriodFormatter(Period period, PeriodFormatter periodFormatter){
        PeriodFormatter hoursMinutesFormatter = periodFormatter;
        Period newPeriod;

        String timeStr;
        if(period.getHours() == 0 && period.getMinutes() < 0){
            newPeriod = new Period( period.getHours(), -period.getMinutes(), 0, 0);
            timeStr = "-" + hoursMinutesFormatter.print(newPeriod);
        }
        else if (period.getHours() < 0 && period.getMinutes() < 0){
            newPeriod = new Period( period.getHours(), -period.getMinutes(), 0, 0);
            timeStr = hoursMinutesFormatter.print(newPeriod);
        }
        else{
            newPeriod = period;
            timeStr = hoursMinutesFormatter.print(newPeriod);
        }
        return  timeStr;
    }

    public static WorkProfile getMinWorkingProfile(Box<WorkProfile> mWorkProfileBox){
        mWorkProfileQuery = mWorkProfileBox.query().order(WorkProfile_.startDate).build();
        WorkProfile wp = mWorkProfileQuery.findFirst();
        return wp;
    }

    public static WorkProfile getMaxWorkingProfile(Box<WorkProfile> mWorkProfileBox){
        mWorkProfileQuery = mWorkProfileBox.query().orderDesc(WorkProfile_.startDate).build();
        WorkProfile wp = mWorkProfileQuery.findFirst();
        return wp;
    }

    public static Duration getDayOfTheWeekWorkingHours(WorkProfile mWorkProfile, Date mDay){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDay);

        switch (calendar.get(Calendar.DAY_OF_WEEK)){
            case Calendar.MONDAY:
                return mWorkProfile.getMonWorkHours();
            case Calendar.TUESDAY:
                return mWorkProfile.getTuesWorkHours();
            case Calendar.WEDNESDAY:
                return  mWorkProfile.getWedWorkHours();
            case Calendar.THURSDAY:
                return mWorkProfile.getThursWorkHours();
            case Calendar.FRIDAY:
                return mWorkProfile.getFriWorkHours();
        }
        return Duration.standardMinutes(0);
    }
}
