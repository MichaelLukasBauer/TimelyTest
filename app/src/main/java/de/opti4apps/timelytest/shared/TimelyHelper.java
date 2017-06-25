package de.opti4apps.timelytest.shared;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.opti4apps.timelytest.App;
import de.opti4apps.timelytest.WorkProfileFragment;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.TotalExtraHours;
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
    private static TotalExtraHours mTotalExtraHours;
    private static Box<TotalExtraHours> mTotalExtraHoursBox;
    private static Query<Day> mDayQuery;
    private static Query<WorkProfile> mWorkProfileQuery;
    private static Query<TotalExtraHours> mTotalExtraHoursQuery;
    private static Duration oldExtrahours;

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



}
