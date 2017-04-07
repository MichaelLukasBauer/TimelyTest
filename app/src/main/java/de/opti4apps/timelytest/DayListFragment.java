package de.opti4apps.timelytest;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.*;
import android.support.v7.widget.DividerItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.event.DayDatasetChangedEvent;
import de.opti4apps.timelytest.event.DaySelectedEvent;
import io.objectbox.Box;
import io.objectbox.query.Query;

/**
 * A fragment representing a list of Days.
 * <p/>
 */
public class DayListFragment extends Fragment {

    public static final String TAG = DayListFragment.class.getSimpleName();

    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    @BindView(R.id.fab)
    FloatingActionButton mFloatingActionButton;

    private final List<Day> mDayList = new ArrayList<>();
    private Box<Day> mDayBox;
    private Query<Day> mDayQuery;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDayBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Day.class);
        mDayQuery = mDayBox.query().build();
        EventBus.getDefault().register(this);

        mDayList.addAll(mDayQuery.find());
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_list, container, false);
        ButterKnife.bind(this,view);
        // if (view instanceof RecyclerView) {
        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(new MyDayRecyclerViewAdapter(mDayList));
        mRecyclerView.setHasFixedSize(true);

        //}
        return view;
    }

    @Override
    public void onDestroyView(){
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @OnClick(R.id.fab)
    public void onFABClicked(){
        EventBus.getDefault().post(new DaySelectedEvent(0));
    }

    @Subscribe
    public void onDayDataSetChanged(DayDatasetChangedEvent event){
        mDayList.clear();
        mDayList.addAll(mDayQuery.find());
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }


}
