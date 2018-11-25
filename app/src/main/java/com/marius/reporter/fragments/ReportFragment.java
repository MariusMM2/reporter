package com.marius.reporter.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.marius.reporter.R;
import com.marius.reporter.Report;
import com.marius.reporter.Report.Time;
import com.marius.reporter.Settings;

import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class ReportFragment extends Fragment {
    private static final String TAG = ReportFragment.class.getSimpleName();
    private AppCompatCheckBox mQuantityLeftLabel;

    private class Arg {
        private static final String REPORT = "report";
    }

    private Settings mSettings;
    private Report mReport;
    private TimeEditor mTimeEditor;

    private TimeAdapter mAdapter;

    private EditText mFlyerNameField;
    private EditText mQuantityLeftField;
    private EditText mGpsNameField;
    private RecyclerView mTimeRecyclerView;
    private FloatingActionButton mAddTimeButton;
    private FloatingActionButton mShareReportButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(getActivity());

        setHasOptionsMenu(true);
        mTimeEditor = new TimeEditor();

        loadReport();

        mReport.setGPSName(mSettings.gpsName);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);

        mTimeEditor.init(v);

        mFlyerNameField = v.findViewById(R.id.flyer_name);
        mFlyerNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mReport.setFlyerName(s.toString());
            }
        });

        mQuantityLeftLabel = v.findViewById(R.id.remaining_flyers_label);
        mQuantityLeftLabel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mReport.setWithRemainingFlyers(isChecked);
            mQuantityLeftField.setEnabled(isChecked);
        });

        mQuantityLeftField = v.findViewById(R.id.remaining_flyers_field);
        mQuantityLeftField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    mReport.setRemainingFlyers(Short.parseShort(s.toString()));
                } catch (NumberFormatException e) {
                    mReport.setRemainingFlyers(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mGpsNameField = v.findViewById(R.id.gps_name);
        mGpsNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mReport.setGPSName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                mSettings.gpsName = s.toString();
            }
        });

        mAddTimeButton = v.findViewById(R.id.add_time_button);
        mAddTimeButton.setOnClickListener(v1 -> {
            mReport.addTime();
            mAdapter.notifyItemInserted(mAdapter.getItemCount()-1);
        });

        mShareReportButton = v.findViewById(R.id.share_report_button);
        mShareReportButton.setOnClickListener(v12 -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, getReportOutput());
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_subject));
            i = Intent.createChooser(i, getString(R.string.send_report));
            startActivity(i);
        });

        mTimeRecyclerView = v.findViewById(R.id.times_recycler_view);
        mTimeRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset_report:
                mReport = new Report();
                mReport.setGPSName(mSettings.gpsName);
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        saveReport();
    }

    private void saveReport() {
        try (
                FileOutputStream fos = Objects.requireNonNull(getActivity()).openFileOutput("currentReport", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(mReport);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadReport() {
        mReport = new Report();

        try (
                FileInputStream fis = Objects.requireNonNull(getActivity()).openFileInput("currentReport");
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            mReport = (Report) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentTime(Time time, TextView timeHolderTextView) {
        mTimeEditor.setCurrentTime(time, timeHolderTextView);
    }

    private void updateUI() {
        mFlyerNameField.setText(mReport.getFlyerName());
        mQuantityLeftLabel.setChecked(mReport.isWithRemainingFlyers());
        mQuantityLeftField.setEnabled(mReport.isWithRemainingFlyers());
        mQuantityLeftField.setText(String.valueOf(mReport.getRemainingFlyers()));
        mGpsNameField.setText(mReport.getGPSName());

        if (mAdapter == null) {
            mAdapter = new TimeAdapter(mReport.getTimes());
            mTimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setTimes(mReport.getTimes());
            mAdapter.notifyDataSetChanged();
        }
    }

    private String getReportOutput() {
        StringBuilder outputBuilder = new StringBuilder();
        String flyerName = getString(R.string.output_flyer_name, mReport.getFlyerName());
        String quantityLeft = getString(R.string.output_quantity_left, mReport.getRemainingFlyers());
        String gpsName = getString(R.string.output_gps_name, mReport.getGPSName());
        String gps = getString(R.string.output_gps, mReport.getTimesString());

        outputBuilder.append(flyerName).append("\n\n");
        if (mReport.isWithRemainingFlyers()) {
            outputBuilder.append(quantityLeft).append("\n\n");
        }
        outputBuilder.append(gpsName).append("\n\n")
                .append(gps);
        return outputBuilder.toString();
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
