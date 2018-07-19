package de.opti4apps.timelytest;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.Day_;
import de.opti4apps.timelytest.data.WorkProfile;
import de.opti4apps.timelytest.data.WorkProfile_;
import de.opti4apps.timelytest.event.DayDatasetChangedEvent;
import de.opti4apps.timelytest.event.DayMultibleSelectionEvent;
import de.opti4apps.timelytest.event.DaySelectedEvent;
import de.opti4apps.timelytest.shared.TrackerHelper;
import de.opti4apps.tracker.gesture.GestureTracker;
import io.objectbox.Box;
import io.objectbox.query.Query;

/**
 * A fragment representing a list of Days.
 * <p/>
 */
public class DayListFragment extends Fragment {

    public static final String TAG = DayListFragment.class.getSimpleName();
    private TrackerHelper tracker;
    private static final String ARG_USER_ID = "userID";
    private final List<Day> mDayList = new ArrayList<>();
    private final String[] mMonthArray = new String[12];
    private final String[] mYearArray = new String[35];
    @BindView(R.id.list)
    RecyclerView mRecyclerView;
    @BindView(R.id.fab)
    FloatingActionButton mFloatingActionButton;
    @BindView(R.id.year_spinner)
    Spinner mYearSpinner;
    @BindView(R.id.month_spinner)
    Spinner mMonthSpinner;
    private Box<Day> mDayBox;
    private Query<Day> mDayQuery;
    private WorkProfile mWorkProfile;
    private Box<WorkProfile> mWorkProfileBox;
    private Query<WorkProfile> mWorkProfileQuery;
    private ActionMode mActionMode;
    private ActionModeCallback mActionModeCallback = new ActionModeCallback();
    private int mCurrentMonthArrayPosition = -1;
    private int mCurrentYearArrayPosition = -1;
    long userID;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DayListFragment() {
    }

    @SuppressWarnings("unused")
    public static DayListFragment newInstance() {
        return new DayListFragment();
    }
    public static DayListFragment newInstance(long userID) {
        DayListFragment dayListfragment = new DayListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userID);
        dayListfragment.setArguments(args);
        return dayListfragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker = new TrackerHelper(TAG,getContext());

        mDayBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Day.class);
        mWorkProfileBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(WorkProfile.class);
        userID = getArguments().getLong(ARG_USER_ID);
        mDayQuery = mDayBox.query().equal(Day_.userID,userID).orderDesc(Day_.day).build();
        EventBus.getDefault().register(this);

        mDayList.addAll(mDayQuery.find());
        setHasOptionsMenu(true);
        setRetainInstance(true);
        initArrays();

    }

    @Override
    public void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);
        tracker.onStartTrack();
    }

    @Override
    public void onStop() {
        super.onStop();
        //EventBus.getDefault().unregister(this);
        tracker.onStopTrack();
    }

    private void initArrays() {
        LocalDate date;

        for (int i = 0; i < mMonthArray.length; i++) {
            date = new LocalDate(0, i + 1, 1);
            mMonthArray[i] = date.monthOfYear().getAsShortText();
        }

        for (int i = 0; i < mYearArray.length; i++) {
            date = LocalDate.now().minusYears(10).plusYears(i);
            mYearArray[i] = date.year().getAsShortText();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_list, container, false);
        ButterKnife.bind(this, view);

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                GestureTracker.trackGesture(getContext(),event,(ViewGroup) v);
                return true;
            }
        });

        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(new MyDayRecyclerViewAdapter(mDayList));
        mRecyclerView.setHasFixedSize(true);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mMonthArray);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMonthSpinner.setAdapter(monthAdapter);
        if (mCurrentMonthArrayPosition == -1)
            mCurrentMonthArrayPosition = monthAdapter.getPosition(LocalDate.now().monthOfYear().getAsShortText());
        mMonthSpinner.setSelection(mCurrentMonthArrayPosition);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mYearArray);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYearSpinner.setAdapter(yearAdapter);
        if (mCurrentYearArrayPosition == -1)
            mCurrentYearArrayPosition = yearAdapter.getPosition(LocalDate.now().year().getAsShortText());
        mYearSpinner.setSelection(mCurrentYearArrayPosition);

        return view;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @OnClick(R.id.fab)
    public void onFABClicked() {
        mWorkProfileQuery = mWorkProfileBox.query().equal(WorkProfile_.userID,userID).build();
        tracker.interactionTrack(getActivity().findViewById(R.id.fab), tracker.getInteractionClicID());
        mWorkProfileQuery = mWorkProfileBox.query().build();
        List<WorkProfile> allWP = mWorkProfileQuery.find();
        if(allWP.size()== 0)
        {
            String message = getResources().getString(R.string.no_working_profile);
            Log.d(TAG,  message);
            Toast.makeText(getActivity(), message,
                    Toast.LENGTH_LONG).show();
        }
        else
        {
            EventBus.getDefault().post(new DaySelectedEvent(0));
        }
    }

    @OnItemSelected(R.id.month_spinner)
    public void onMonthSpinnerItemSelected(Spinner spinner, int position) {
        tracker.interactionTrack(getActivity().findViewById(R.id.month_spinner), tracker.getInteractionClicID());
        mCurrentMonthArrayPosition = position;
        spinnerItemSelected();
    }

    @OnItemSelected(R.id.year_spinner)
    public void onYearSpinnerItemSelected(Spinner spinner, int position) {
        tracker.interactionTrack(getActivity().findViewById(R.id.year_spinner), tracker.getInteractionClicID());
        mCurrentYearArrayPosition = position;
        spinnerItemSelected();
    }

    private void spinnerItemSelected() {
        String dateString = mMonthSpinner.getAdapter().getItem(mCurrentMonthArrayPosition).toString() + mYearSpinner.getAdapter().getItem(mCurrentYearArrayPosition).toString();
        Log.d(TAG, dateString);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMyyyy");
        DateTime dateTime = DateTime.parse(dateString, formatter);
        Date min = dateTime.withDayOfMonth(1).toDate();
        Date max = dateTime.withDayOfMonth(1).plusMonths(1).minusDays(1).toDate();
        mDayQuery = mDayBox.query().equal(Day_.userID,userID).between(Day_.day, min, max).orderDesc(Day_.day).build();
        EventBus.getDefault().post(new DayDatasetChangedEvent(TAG));
    }

    @OnClick ({R.id.imageDate,R.id.imageLogin,R.id.imageLogout,R.id.imagePause,R.id.imageTime,R.id.list})
    public void clickUnEditableLabelsImages(View v) {
        int mSelectedText;
        mSelectedText = v.getId();
        if (mSelectedText == R.id.imageDate)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.imageDate), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.imageLogin)
        {
            tracker.interactionTrack( getActivity().findViewById(R.id.imageLogin), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.imageLogout)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.imageLogout), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.imagePause)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.imagePause), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.imageTime)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.imageTime), tracker.getInteractionClicID());
        }
        else if (mSelectedText == R.id.list)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.list), tracker.getInteractionClicID());
        }
    }

    @Subscribe
    public void onDayDataSetChanged(DayDatasetChangedEvent event) {
        mDayList.clear();
        mDayList.addAll(mDayQuery.find());
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Subscribe
    public void onDayMultibleSelection(DayMultibleSelectionEvent event) {
        Log.d(TAG, "onDayMultibleSelection: selection size = " + event.selectionSize);
        if (event.selectionSize > 0 && mActionMode == null) {
            ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);

        } else if (event.selectionSize <= 0) {
            mActionMode.finish();
        }

    }

    private void clearSelection() {

        for (Map.Entry<Day, Integer> entry : ((MyDayRecyclerViewAdapter) mRecyclerView.getAdapter()).getSelection().entrySet()) {
            CardView view = (CardView) mRecyclerView.getLayoutManager().findViewByPosition(entry.getValue());
            if (view != null) {
                view.setActivated(false);
                view.setCardBackgroundColor(Color.WHITE);
            }
        }
        ((MyDayRecyclerViewAdapter) mRecyclerView.getAdapter()).getSelection().clear();
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            mode.getMenuInflater().inflate(R.menu.menu_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {

                case R.id.delete:
                    tracker.interactionTrack(getActivity().findViewById(R.id.delete), tracker.getInteractionActionID());
                    mDayBox.remove(((MyDayRecyclerViewAdapter) mRecyclerView.getAdapter()).getSelection().keySet());
                    clearSelection();
                    EventBus.getDefault().post(new DayDatasetChangedEvent(TAG));
                    Log.d(TAG, "menu_remove");
                    mode.finish();
                    return true;

                default:

                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            clearSelection();
            mActionMode = null;
        }
    }
}
