package de.opti4apps.timelytest;

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

import java.util.List;

import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.event.DaySelectedEvent;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Day} and makes a call to the
 * specified {@link DayListFragment.OnDayListFragmentInteractionListener}.
 */
public class MyDayRecyclerViewAdapter extends RecyclerView.Adapter<MyDayRecyclerViewAdapter.ViewHolder> {

    private final List<Day> mDays;

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

    }

    @Override
    public int getItemCount() {
        return mDays.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mDate;
        public final TextView mCheckIn;
        public final TextView mCheckOut;
        public final TextView mPause;
        public final TextView mTotalTime;

        public View mView;
        public Day mDay;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDate = (TextView) view.findViewById(R.id.dateItem);
            mCheckIn = (TextView) view.findViewById(R.id.checkinItem);
            mCheckOut = (TextView) view.findViewById(R.id.checkoutItem);
            mPause = (TextView) view.findViewById(R.id.pauseItem);
            mTotalTime = (TextView) view.findViewById(R.id.totalTimeItem);
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
            DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");
            DateTimeFormatter fmtTime = DateTimeFormat.forPattern("HH:mm");

            String dateAt = "", dayAt = "", checkinAt = "", checkoutAt = "", pauseAt = "", totalTimeAt = "";

            dateAt = (day.getDay().dayOfMonth().getAsShortText() + " " + day.getDay().monthOfYear().getAsShortText());
            dayAt = (day.getDay().dayOfWeek().getAsShortText());
            checkinAt = day.getStart().toString(fmtTime);
            checkoutAt = day.getEnd().toString(fmtTime);

            PeriodFormatter hoursMinutes = new PeriodFormatterBuilder()
                    .appendHours()
                    .appendSuffix("h ")
                    .appendMinutes()
                    .appendSuffix("min")
                    .toFormatter();

            pauseAt = day.getPause().toPeriod().toString(hoursMinutes);
            totalTimeAt = day.getTotalWorkingTime().toPeriod().toString(hoursMinutes);

            mDate.setText(dayAt + "\n" + dateAt);
            mCheckIn.setText(checkinAt);
            mCheckOut.setText(checkoutAt);
            mPause.setText(pauseAt);
            mTotalTime.setText(totalTimeAt);

        }
    }
}
