package de.opti4apps.timelytest.shared;

import org.joda.time.Duration;

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
        mWorkProfileQuery = mWorkProfileBox.query().build();
        List<WorkProfile> allWP = mWorkProfileQuery.find();
        for (WorkProfile wp: allWP)
        {
            if (isDayInWokingProfile(d,wp))
            {
                return wp;
            }
        }

        return null;
    }

    public static boolean isDayInWokingProfile(Day d, WorkProfile wp)
    {
        if (d.getDay().getMillis() >= wp.getStartDate().getMillis() && d.getDay().getMillis() <= wp.getEndDate().getMillis())
        {
            return true;
        }
        return false;
    }

    public static long getTotalOvertime(Box<Day> mDayBox, Box<WorkProfile> mWorkProfileBox)
    {
        long totalOvertime = 0;
        mDayQuery = mDayBox.query().build();
        List<Day> allDay = mDayQuery.find();
        for (Day d: allDay)
        {
            WorkProfile wp = getValidWorkingProfile(d,mWorkProfileBox);
            d.computeTheExtraHours(wp);
            totalOvertime += d.getExtraHours().getMillis();
        }
        return totalOvertime;
    }

}
