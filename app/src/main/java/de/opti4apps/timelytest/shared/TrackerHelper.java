package de.opti4apps.timelytest.shared;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import java.util.HashMap;
import de.opti4apps.tracker.interaction.InteractionTracker;


/**
 * Created by TCHATCHO on 08.03.2018.
 */

public class TrackerHelper {
    private HashMap<String,String> payload = new HashMap<String,String>(1,0.75F);
    private String activityName;
    private static final String VIEW_TYPE_INDICATOR = "AppCompat";
    private static final String VIEW_ID_INDICATOR = "id/";
    private Context context;
    private long userID = 0;

    public static final String SIGN_IN = "SIGN_IN";

    public static final String WORk_PROFILE = "WORK_PROFILE";
    public static final String WORk_PROFILE_GUID = "760ad982-e0fd-244c-090a-a8fb7f60b572";

    public static final String CREATE_DAY = "CREATE_DAY";
    public static final String CREATE_DAY_GUID = "af9044da-957c-97a2-6142-fe98ba2cc36d";

    public static final String DELETE_DAY = "DELETE_DAY";

    public static final String CHANGE_EXISTING_DAY = "CHANGE_EXISTING_DAY";
    public static final String CHANGE_EXISTING_DAY_GUID = "09af8a21-67a8-d958-0fb6-c39a17dc0854";

    public static final String MONTH_OVERVIEW = "MONTH_OVERVIEW";
    public static final String MONTH_OVERVIEW_GUID = "fba0167a-65ba-a064-7ce1-2776b572d2d0";

    public static final String SEND_GENERATE_REPORT = "SEND_GENERATE_REPORT";
    public static final String SEND_GENERATE_REPORT_GUID = "bf3d4a60-f363-6838-a5ca-6d68187f5a19";

    public static final String SEND_REPORT = "SEND_REPORT";

    public static final String GENERATE_REPORT = "GENERATE_REPORT";

    public static final String NUMBER_INPUT = "NUMBER_INPUT";
    public static final String NUMBER_INPUT_GUID = "d7e4cafb-fc62-fc52-b420-a065031f15b2";

    public static final String HELP = "HELP";

    public TrackerHelper(String activityName,Context context)
    {
        this.activityName = activityName;
        this.context = context;
    }
    public TrackerHelper(String activityName,Context context,long userID )
    {
        this.activityName = activityName;
        this.context = context;
        this.userID = userID;
    }
    private void handlePayload(String StartUserStoryName,String EndUserStoryName, String extraValue)
    {
        if (!StartUserStoryName.isEmpty())
        {
            if(!getGUIValuefromUserStory(StartUserStoryName).isEmpty())
            {
                payload.put("GUID", getGUIValuefromUserStory(StartUserStoryName));
            }
            payload.put("StartUserStoryName", StartUserStoryName);
        }
        if (!EndUserStoryName.isEmpty())
        {
            if(!getGUIValuefromUserStory(EndUserStoryName).isEmpty())
            {
                payload.put("GUID", getGUIValuefromUserStory(EndUserStoryName));
            }
            payload.put("EndUserStoryName", EndUserStoryName);
        }
        if (!extraValue.isEmpty())
        {
            payload.put("Extra", extraValue);
        }
    }

    //public void  addUser

    public void onStartTrack(String StartUserStoryName,String EndUserStoryName, String extraValue)
    {
        payload.clear();
        payload.put("ActivityName",activityName);
        payload.put("UserID", String.valueOf(userID));
        handlePayload(StartUserStoryName,EndUserStoryName,extraValue);
        InteractionTracker.track(context,InteractionTracker.ActionTypes.OPEN,payload);
    }

    public void onStopTrack(String StartUserStoryName,String EndUserStoryName, String extraValue)
    {
        payload.clear();
        payload.put("ActivityName",activityName);
        payload.put("UserID", String.valueOf(userID));
        handlePayload(StartUserStoryName,EndUserStoryName,extraValue);
        InteractionTracker.track(context,InteractionTracker.ActionTypes.CLOSE,payload);
    }

    private String getViewType(View v)
    {
        String viewType = v.getClass().getName().substring(v.getClass().getName().indexOf(VIEW_TYPE_INDICATOR)+VIEW_TYPE_INDICATOR.length());
        return viewType ;
    }
    private String getViewID(View v)
    {
        String  viewID = v.toString().substring(v.toString().indexOf(VIEW_ID_INDICATOR)+VIEW_ID_INDICATOR.length(),v.toString().length()-1);
        return viewID;
    }

    private void setPayload(Object v,String StartUserStoryName,String EndUserStoryName,String extraValue)
    {
        payload.clear();
        payload.put("ActivityName", activityName);
        payload.put("UserID", String.valueOf(userID));
        if (v instanceof  View) {
            payload.put("ViewID", getViewID((View)v));
            payload.put("ViewType", getViewType((View)v));
        }
         else if (v instanceof MenuItem)
        {
            payload.put("MenuName", (String) ((MenuItem)v).getTitle());
            payload.put("MenuType", "MenuItem");
         }

        handlePayload(StartUserStoryName,EndUserStoryName,extraValue);
    }

    public int getInteractionActionID()
    {
        return 0;
    }
    public int getInteractionEventID()
    {
        return 1;
    }
    public int getInteractionClicID()
    {
        return 2;
    }
    public int getInteractionSwipeID()
    {
        return 3;
    }
    public int getInteractionOpenID()
    {
        return 4;
    }
    public int getInteractionCloseID()
    {
        return 5;
    }
    public int getInteractionShownID()
    {
        return 6;
    }
    public int getInteractionStatusChangeID()
    {
        return 7;
    }
    public int getInteractionSystemEventID()
    {
        return 8;
    }
    public int getInteractionTapID()
    {
        return 9;
    }
    public int getInteractionErrorID()
    {
        return 10;
    }
    public int getInteractionSelectID()
    {
        return 11;
    }

    public void interactionTrack(Object v,int interactionID,String StartUserStoryName,String EndUserStoryName,String extraValue)
    {
        setPayload(v,StartUserStoryName,EndUserStoryName,extraValue);

        switch(interactionID)
        {
            case 0:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.ACTION,payload);
                break;
            case 1:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.EVENT,payload);
                break;
            case 2:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.CLICK,payload);
                break;
            case 3:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.SWIPE,payload);
                break;
            case 4:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.OPEN,payload);
                break;
            case 5:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.CLOSE,payload);
                break;
            case 6:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.SHOWN,payload);
                break;
            case 7:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.STATUS_CHANGE,payload);
                break;
            case 8:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.SYSTEM_EVENT,payload);
                break;
            case 9:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.TAP,payload);
                break;
            case 10:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.ERROR,payload);
                break;
            case 11:
                InteractionTracker.track(context,InteractionTracker.ActionTypes.SELECT,payload);
                break;
        }

    }

    private String getGUIValuefromUserStory(String UserStoryName)
    {
        String GUID_value = "";
        if (UserStoryName == WORk_PROFILE)
        {
            GUID_value = WORk_PROFILE_GUID;
        }
        else if (UserStoryName == NUMBER_INPUT )
        {
            GUID_value = NUMBER_INPUT_GUID;
        }
        else if (UserStoryName == MONTH_OVERVIEW )
        {
            GUID_value = MONTH_OVERVIEW_GUID;
        }
        else if (UserStoryName == CHANGE_EXISTING_DAY )
        {
            GUID_value = CHANGE_EXISTING_DAY_GUID;
        }
        else if (UserStoryName == CREATE_DAY )
        {
            GUID_value = CREATE_DAY_GUID;
        }
        else if (UserStoryName == SEND_GENERATE_REPORT || UserStoryName == SEND_REPORT || UserStoryName == GENERATE_REPORT )
        {
            GUID_value = SEND_GENERATE_REPORT_GUID;
        }
        return GUID_value;
    }
}
