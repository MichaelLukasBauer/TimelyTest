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

    public TrackerHelper(String activityName,Context context)
    {
        this.activityName = activityName;
        this.context = context;
    }

    public void onStartTrack()
    {
        payload.clear();
        payload.put("ActivityName",activityName);
        InteractionTracker.track(context,InteractionTracker.ActionTypes.OPEN,payload);
    }

    public void onStopTrack()
    {
        payload.clear();
        payload.put("ActivityName",activityName);
        InteractionTracker.track(context,InteractionTracker.ActionTypes.CLOSE,payload);
    }

    public void setUserScenarioEntryPoint(Object v, String value)
    {
        if (value.isEmpty())
        {
            if (v instanceof  View) {
                value = getViewID((View)v);
            }
            else if (v instanceof MenuItem) {
                value = (String) ((MenuItem)v).getTitle();
            }
        }

        payload.clear();
        payload.put("ActivityName",activityName);
        payload.put("StartUserCase", value);
        InteractionTracker.track(context,InteractionTracker.ActionTypes.EVENT,payload);
    }

    public void setUserScenarioExitPoint(View v)
    {
        payload.clear();
        payload.put("ActivityName",activityName);
        payload.put("EndUserCase", getViewID(v));
        InteractionTracker.track(context,InteractionTracker.ActionTypes.EVENT,payload);
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
    private void setPayload(Object v)
    {
        payload.clear();
        payload.put("ActivityName", activityName);
        if (v instanceof  View) {
            payload.put("ViewID", getViewID((View)v));
            payload.put("ViewType", getViewType((View)v));
        }
        else if (v instanceof MenuItem)
        {
            payload.put("MenuName", (String) ((MenuItem)v).getTitle());
            payload.put("Type", "MenuItem");
        }
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

    public void interactionTrack(Object v,int interactionID)
    {
        setPayload(v);

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
}
