package de.opti4apps.timelytest;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.event.DaySelectedEvent;

import static de.opti4apps.timelytest.R.color.accent_material_dark;
import static de.opti4apps.timelytest.R.color.accent_material_light;
import static de.opti4apps.timelytest.R.color.colorAccent;
import static de.opti4apps.timelytest.R.color.colorPrimaryLight;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Day} and makes a call to the
 * specified {@link DayListFragment}.
 */
public class MyDayRecyclerViewAdapter extends RecyclerView.Adapter<MyDayRecyclerViewAdapter.ViewHolder> {

    private final List<Day> mDays;

    private final Set<Day> mSelection = new HashSet<>();

    public MyDayRecyclerViewAdapter(List<Day> days) {
        mDays = days;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_day_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Day day = mDays.get(position);
        holder.setData(day);


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new DaySelectedEvent(holder.mDay.getId()));
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                CardView view = (CardView) v.findViewById(R.id.cardView);
                if (v.isActivated()) {
                    v.setActivated(false);
                    mSelection.remove(holder.mDay);
                    view.setCardBackgroundColor(Color.WHITE);
                } else {
                    v.setActivated(true);
                    mSelection.add(holder.mDay);
                    view.setCardBackgroundColor(v.getResources().getColor(colorPrimaryLight));

                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDays.size();
    }

    public Set<Day> getSelection() {
        return mSelection;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.dateItem)
        TextView mDate;
        @BindView(R.id.checkinItem)
        TextView mCheckIn;
        @BindView(R.id.checkoutItem)
        TextView mCheckOut;
        @BindView(R.id.pauseItem)
        TextView mPause;
        @BindView(R.id.totalTimeItem)
        TextView mTotalTime;

        View mView;
        Day mDay;

        ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, mView);
        }

        @Override
        public String toString() {
            return super.toString()
                    + " '" + mDate.getText() + "'"
                    + " '" + mCheckIn.getText() + "'"
                    + " '" + mCheckOut.getText() + "'"
                    + " '" + mPause.getText() + "'"
                    + " '" + mTotalTime.getText() + "'";
        }

        public void setData(Day day) {
            mDay = day;

            String dateAt = "", dayAt = "", checkinAt = "", checkoutAt = "", pauseAt = "", totalTimeAt = "";

            dateAt = (day.getDay().dayOfMonth().getAsShortText() + " " + day.getDay().monthOfYear().getAsShortText());
            dayAt = (day.getDay().dayOfWeek().getAsShortText());
            checkinAt = day.getStart().toString(Day.TIME_FORMATTER);
            checkoutAt = day.getEnd().toString(Day.TIME_FORMATTER);
            pauseAt = day.getPause().toPeriod().toString(Day.PERIOD_FORMATTER);
            totalTimeAt = day.getTotalWorkingTime().toPeriod().toString(Day.PERIOD_FORMATTER);

            mDate.setText(dayAt + "\n" + dateAt);
            mCheckIn.setText(checkinAt);
            mCheckOut.setText(checkoutAt);
            mPause.setText(pauseAt);
            mTotalTime.setText(totalTimeAt);

        }
    }
}
