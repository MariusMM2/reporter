package com.marius.reporter.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.marius.reporter.R;
import com.marius.reporter.Report;
import com.marius.reporter.Report.Time;

import java.util.List;

public class ReportFragment extends Fragment {
    private Report mReport;
    private NumberPicker mHourPicker;
    private NumberPicker mMinutePicker;
    private NumberPicker mSecondPicker;
    private Time mTime;
    private TimeAdapter mAdapter;

    private TextView mTimeHolderTextView;
    private RecyclerView mTimeRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReport = new Report();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);

        mHourPicker = v.findViewById(R.id.hour_picker);
        mMinutePicker = v.findViewById(R.id.minute_picker);
        mSecondPicker = v.findViewById(R.id.second_picker);

        mHourPicker.setMaxValue(23);
        mMinutePicker.setMaxValue(59);
        mSecondPicker.setMaxValue(59);

        mHourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            mTime.setHours(newVal);
            if (mTimeHolderTextView != null)
                mTimeHolderTextView.setText(mTime.toString());
        });
        mMinutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            mTime.setMinutes(newVal);
            if (mTimeHolderTextView != null)
                mTimeHolderTextView.setText(mTime.toString());
        });
        mSecondPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            mTime.setSeconds(newVal);
            if (mTimeHolderTextView != null)
                mTimeHolderTextView.setText(mTime.toString());
        });

        mTime = new Time();
        mReport.addTime(10, 10, 10);
        mReport.addTime(20, 20, 20);
        mReport.addTime(10, 10, 10);
        mReport.addTime(20, 20, 20);
        mReport.addTime(10, 10, 10);
        mReport.addTime(20, 20, 20);
        mReport.addTime(10, 10, 10);
        mReport.addTime(20, 20, 20);
        mReport.addTime(10, 10, 10);
        mReport.addTime(20, 20, 20);
        mTimeRecyclerView = v.findViewById(R.id.times_recycler_view);
        mTimeRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        updateUI();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void setCurrentTime(Time time, TextView timeHolderTextView) {
        mTime = time;
        mHourPicker.setValue(mTime.getHours());
        mMinutePicker.setValue(mTime.getMinutes());
        mSecondPicker.setValue(mTime.getSeconds());
        mTimeHolderTextView = timeHolderTextView;
    }

    public void updateUI() {
        List<Time> times = mReport.getTimes();

        if (mAdapter == null) {
            mAdapter = new TimeAdapter(times);
            mTimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setTimes(times);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class TimeHolder extends RecyclerView.ViewHolder {
        private Time mTime;
        private TextView mTimeTextView;

        public TimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_time, parent, false));
            mTimeTextView = itemView.findViewById(R.id.time_text);
            itemView.setOnClickListener(v -> setCurrentTime(mTime, mTimeTextView));
        }

        public void bind(Time time) {
            mTime = time;
            mTimeTextView.setText(mTime.toString());
        }
    }

    private class TimeAdapter extends RecyclerView.Adapter<TimeHolder> {
        private List<Time> mTimes;

        public TimeAdapter(List<Time> times) {
            mTimes = times;
        }

        @Override
        public void onBindViewHolder(@NonNull TimeHolder holder, int position, @NonNull List<Object> payloads) {
            Time time = mTimes.get(position);
            holder.bind(time);
        }

        @NonNull
        @Override
        public TimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new TimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull TimeHolder timeHolder, int i) {}

        @Override
        public int getItemCount() {
            return mTimes.size();
        }

        public void setTimes(List<Time> times) {
            mTimes = times;
        }
    }
}
