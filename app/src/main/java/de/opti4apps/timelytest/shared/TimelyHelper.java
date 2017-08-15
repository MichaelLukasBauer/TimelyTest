package de.opti4apps.timelytest.shared;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.List;

import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.WorkProfile;
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

    public static WorkProfile getValidWorkingProfile(Day d,Box<WorkProfile> mWorkProfileBox)
    {
        WorkProfile resultWorkProfile = null;
        mWorkProfileQuery = mWorkProfileBox.query().build();
        List<WorkProfile> allWP = mWorkProfileQuery.find();
        for (WorkProfile wp: allWP)
        {
            if (isDayInWokingProfile(d,wp))
            {
                resultWorkProfile = wp;
                break;
            }
        }

        if(allWP.size() > 0 && (d.getDay().withTimeAtStartOfDay().getMillis() > allWP.get(allWP.size()-1).getEndDate().withTimeAtStartOfDay().getMillis()))
        {
            resultWorkProfile = allWP.get(allWP.size()-1);
        }

        return resultWorkProfile;
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

    public static long getTotalOvertime(Box<Day> mDayBox, Box<WorkProfile> mWorkProfileBox)
    {
        long totalOvertime = 0;
        mDayQuery = mDayBox.query().build();
        List<Day> allDay = mDayQuery.find();
        for (Day d: allDay)
        {
            WorkProfile wp = getValidWorkingProfile(d,mWorkProfileBox);
            if (wp == null) {
                wp  = new WorkProfile(0, Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0), Duration.standardMinutes(0));
            }
            d.computeTheExtraHours(wp);
            totalOvertime += d.getExtraHours().getMillis();
        }
        return totalOvertime;
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

}
