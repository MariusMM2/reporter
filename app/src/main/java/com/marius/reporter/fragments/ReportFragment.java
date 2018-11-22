package com.marius.reporter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.*;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.marius.reporter.R;
import com.marius.reporter.Report;
import com.marius.reporter.Report.Time;
import com.marius.reporter.activities.ReportActivity;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ReportFragment extends Fragment {
    private Report mReport;
    private TimeEditor mTimeEditor;

    private TimeAdapter mAdapter;
    private RecyclerView mTimeRecyclerView;
    private FloatingActionButton mAddTimeButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mReport = (Report) savedInstanceState.getSerializable(Arg.REPORT);
        }
        setHasOptionsMenu(true);
        mTimeEditor = new TimeEditor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);

        mTimeEditor.init(v);

        mTimeRecyclerView = v.findViewById(R.id.times_recycler_view);
        mTimeRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        mAddTimeButton = v.findViewById(R.id.add_time_button);
        mAddTimeButton.setOnClickListener(v1 -> {
            mReport.addTime();
            mAdapter.notifyItemInserted(mAdapter.getItemCount()-1);
        });

        updateUI();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(mAdapter));
        itemTouchHelper.attachToRecyclerView(mTimeRecyclerView);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_report, menu);

        SwitchCompat dayNightSwitch = menu.findItem(R.id.day_night_switch).getActionView().findViewById(R.id.menu_switch);
        dayNightSwitch.setChecked(ReportActivity.isDarkMode);
        dayNightSwitch.animate();
        dayNightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ReportActivity.isDarkMode = isChecked;

            Intent intent = new Intent(getActivity(), ReportActivity.class);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startActivity(intent);
                    getActivity().finish();
                    timer.cancel();
                }
            }, 220);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Arg.REPORT, mReport);
    }

    private void setCurrentTime(Time time, TextView timeHolderTextView) {
        mTimeEditor.setCurrentTime(time, timeHolderTextView);
    }

    private void updateUI() {

        if (mAdapter == null) {
            mAdapter = new TimeAdapter(mReport.getTimes());
            mTimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private static class TimeEditor {
        private NumberPicker mHourPicker;
        private NumberPicker mMinutePicker;
        private NumberPicker mSecondPicker;
        private Time mTime;

        private TextView mTimeHolderTextView;

        public TimeEditor() {
            mTime = new Time();
        }

        public void init(View parent) {
            mHourPicker = parent.findViewById(R.id.hour_picker);
            mMinutePicker = parent.findViewById(R.id.minute_picker);
            mSecondPicker = parent.findViewById(R.id.second_picker);

            mHourPicker.setMaxValue(23);
            mMinutePicker.setMaxValue(59);
            mSecondPicker.setMaxValue(59);
            mHourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                mTime.setHours(newVal);
                updateText();
            });
            mMinutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                mTime.setMinutes(newVal);
                updateText();
            });
            mSecondPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                mTime.setSeconds(newVal);
                updateText();
            });
            mHourPicker.setFormatter(TimeEditor::format);
            mMinutePicker.setFormatter(TimeEditor::format);
            mSecondPicker.setFormatter(TimeEditor::format);

            mHourPicker.setEnabled(false);
            mMinutePicker.setEnabled(false);
            mSecondPicker.setEnabled(false);
        }

        public static String format(int value) {
            return String.format(Locale.UK, "%02d", value);
        }

        public void setCurrentTime(Time time, TextView timeHolderTextView) {
            mTime = time;
            mHourPicker.setValue(mTime.getHours());
            mMinutePicker.setValue(mTime.getMinutes());
            mSecondPicker.setValue(mTime.getSeconds());

            if (!mHourPicker.isEnabled()) mHourPicker.setEnabled(true);
            if (!mMinutePicker.isEnabled()) mMinutePicker.setEnabled(true);
            if (!mSecondPicker.isEnabled()) mSecondPicker.setEnabled(true);

            mTimeHolderTextView = timeHolderTextView;
        }

        private void updateText() {
            if (mTimeHolderTextView != null)
                mTimeHolderTextView.setText(mTime.toString());
        }
    }

    private class TimeHolder extends RecyclerView.ViewHolder {
        private Time mTime;
        private TextView mTimeTextView;

        public TimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_time, parent, false));
            mTimeTextView = itemView.findViewById(R.id.item_time_text);
            itemView.setOnClickListener(v -> setCurrentTime(mTime, mTimeTextView));
        }

        public void bind(Time time) {
            mTime = time;
            mTimeTextView.setText(mTime.toString());
        }
    }

    private class TimeAdapter extends RecyclerView.Adapter<TimeHolder> {
        private List<Time> mTimes;
        private Time mRecentlyDeletedTime;
        private int mRecentlyDeletedTimePosition;

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

        public void deleteTime(int position) {
            mRecentlyDeletedTime = mTimes.remove(position);
            mRecentlyDeletedTimePosition = position;

            notifyItemRemoved(position);
            showUndoSnackbar();
        }

        private void showUndoSnackbar() {
            View view = getActivity().findViewById(R.id.constraint_layout);
            Snackbar snackbar = Snackbar.make(view, "1 Time Deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", v -> undoDelete());
            snackbar.show();
        }

        private void undoDelete() {
            mTimes.add(mRecentlyDeletedTimePosition, mRecentlyDeletedTime);
            notifyItemInserted(mRecentlyDeletedTimePosition);
        }
    }

    class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private TimeAdapter mAdapter;

        public SwipeToDeleteCallback(TimeAdapter adapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            mAdapter = adapter;

        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            int position = viewHolder.getAdapterPosition();

            mAdapter.deleteTime(position);
        }


    }
}
