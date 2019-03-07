package de.opti4apps.timelytest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import butterknife.BindView;
import butterknife.ButterKnife;

import de.opti4apps.timelytest.shared.TrackerHelper;
import de.opti4apps.tracker.gesture.GestureTracker;

/**
 * Created by TCHATCHO on 06.03.2019.
 */

public class HelpFragment extends Fragment {
    public static final String TAG = HelpFragment.class.getSimpleName();
    private static final String ARG_HELP_ID = "helpID";
    private static final String ARG_USER_ID = "userID";
    private TrackerHelper tracker;
    @BindView(R.id.fragment_help_title)
    TextView mFragHelptTle;

    @BindView(R.id.capture_time_help_title)
    TextView mCapTimeHelpTtle;

    @BindView(R.id.capture_time_help_desc)
    TextView mCapTimeHelpDesc;

    @BindView(R.id.work_profile_help_title)
    TextView mWorkProfHelpTtle;

    @BindView(R.id.work_profile_help_desc)
    TextView mWorkProfHelpDesc;

    @BindView(R.id.month_overview_help_title)
    TextView mMonthOveHelpTtle;

    @BindView(R.id.month_overview_help_desc)
    TextView mMonthOveHelpDesc;

    @BindView(R.id.create_time_sheet_help_title)
    TextView mCreTimeSheetHelpTtle;

    @BindView(R.id.create_time_sheet_help_desc)
    TextView mCreTimeSheetHelpDesc;

    long userID;

    private ActionMode mActionMode;
    private HelpFragment.ActionModeCallback mActionModeCallback = new HelpFragment.ActionModeCallback();
    public HelpFragment() { }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userID the ID of the current user.
     * @return A new instance of fragment WorkProfileFragment.
     */
    public static HelpFragment newInstance(long userID) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userID);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userID = getArguments().getLong(ARG_USER_ID);
        tracker = new TrackerHelper(TAG,getContext(),userID);


        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_help, container, false);
        ButterKnife.bind(this, view);

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                GestureTracker.trackGesture(getContext(),event,(ViewGroup) v);
                return true;
            }
        });

        //((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        tracker.onStartTrack("","","");
    }

    @Override
    public void onStop() {
        super.onStop();
        tracker.onStopTrack("","","");
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @SuppressWarnings("unused")
        private final String TAG = HelpFragment.ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            mode.getMenuInflater().inflate(R.menu.capture_time_menu, menu);
            mActionMode.setTitle(getResources().getString(R.string.app_name));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getActivity().onBackPressed();
            //mActionMode = null;
        }
    }
}
