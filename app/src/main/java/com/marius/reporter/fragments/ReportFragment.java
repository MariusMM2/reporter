package com.marius.reporter.fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
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
public class ReportFragment extends Fragment implements Report.Callbacks{
    @SuppressWarnings("unused")
    private static final String TAG = ReportFragment.class.getSimpleName();

    @SuppressWarnings("unused")
    private class Arg {
        private static final String REPORT = "report";
    }

    private Settings mSettings;
    private Report mReport;
    private TimeEditor mTimeEditor;
    private float mShareFabY;

    private TimeAdapter mAdapter;

    private EditText mFlyerNameField;
    private AppCompatCheckBox mQuantityLeftLabel;
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

        mFlyerNameField = v.findViewById(R.id.flyer_name);
        mQuantityLeftLabel = v.findViewById(R.id.remaining_flyers_label);
        mQuantityLeftField = v.findViewById(R.id.remaining_flyers_field);
        mGpsNameField = v.findViewById(R.id.gps_name);
        mAddTimeButton = v.findViewById(R.id.add_time_button);
        mShareReportButton = v.findViewById(R.id.share_report_button);

        mShareFabY = mShareReportButton.getTop();

        mTimeEditor.init(v);

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
                updateShareFAB();
            }
        });
        mQuantityLeftLabel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mReport.setWithRemainingFlyers(isChecked);
            mQuantityLeftField.setEnabled(isChecked);
            updateShareFAB();
        });
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
                updateShareFAB();
            }
        });
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

                updateShareFAB();
            }
        });
        mAddTimeButton.setOnClickListener(v1 -> {
            mReport.add(new Time());
            mAdapter.notifyItemInserted(mAdapter.getItemCount()-1);
            updateShareFAB();
        });
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

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset_report:
                mReport = new Report(this);
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
        mReport = new Report(this);

        try (
                FileInputStream fis = Objects.requireNonNull(getActivity()).openFileInput("currentReport");
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            mReport = (Report) ois.readObject();
            mReport.setCallBacks(this);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentTime(Time time, CardView timeHolderCard) {
        mTimeEditor.setCurrentTime(time, timeHolderCard);
    }

    private void updateUI() {
        mFlyerNameField.setText(mReport.getFlyerName());
        mQuantityLeftLabel.setChecked(mReport.isWithRemainingFlyers());
        mQuantityLeftField.setEnabled(mReport.isWithRemainingFlyers());
        mQuantityLeftField.setText(String.valueOf(mReport.getRemainingFlyers()));
        mGpsNameField.setText(mReport.getGPSName());
        updateShareFAB();

        if (mAdapter == null) {
            mAdapter = new TimeAdapter(mReport);
            mTimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setReport(mReport);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void updateShareFAB() {
        boolean isReady = mReport.isReadyToSend();
        final int duration = 500;
        if (isReady) {
            mShareReportButton.animate().translationY(mShareFabY).setDuration(duration).setInterpolator(new OvershootInterpolator()).start();
        } else {
            mShareReportButton.animate().translationY(mAddTimeButton.getTop()-mShareReportButton.getTop()).setDuration(duration).setInterpolator(new AnticipateInterpolator()).start();
        }
    }

    @Override
    public void onListUpdated() {
        updateShareFAB();
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

        private CardView mTimeHolderCard;

        TimeEditor() {
            mTime = new Time();
        }

        void init(View parent) {
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

        static String format(int value) {
            return String.format(Locale.UK, "%02d", value);
        }

        void setCurrentTime(Time time, CardView timeHolderCard) {
            if (mTimeHolderCard == timeHolderCard) return;

            elevateCard(timeHolderCard, R.dimen.item_time_selected);

            if (mTimeHolderCard != null)
                elevateCard(mTimeHolderCard, R.dimen.item_time_normal);

            mTime = time;
            mHourPicker.setValue(mTime.getHours());
            mMinutePicker.setValue(mTime.getMinutes());
            mSecondPicker.setValue(mTime.getSeconds());

            if (!mHourPicker.isEnabled()) mHourPicker.setEnabled(true);
            if (!mMinutePicker.isEnabled()) mMinutePicker.setEnabled(true);
            if (!mSecondPicker.isEnabled()) mSecondPicker.setEnabled(true);

            mTimeHolderCard = timeHolderCard;
        }

        private void elevateCard(@NonNull CardView timeHolderCard, @DimenRes int item_time_elevation) {
            ValueAnimator valueAnimator = new ValueAnimator();
            valueAnimator.setInterpolator(new OvershootInterpolator());
            valueAnimator.setFloatValues(timeHolderCard.getElevation(), timeHolderCard.getResources().getDimension(item_time_elevation));
            valueAnimator.setDuration(1000);
            valueAnimator.addUpdateListener(animation -> timeHolderCard.setElevation((float) animation.getAnimatedValue()));
            valueAnimator.start();
        }

        private void updateText() {
            if (mTimeHolderCard != null)
                ((TextView) mTimeHolderCard.findViewById(R.id.item_time_text)).setText(mTime.toString());
        }
    }

    private class TimeHolder extends RecyclerView.ViewHolder {
        private Time mTime;

        TimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_time, parent, false));
            itemView.setOnClickListener(v -> ReportFragment.this.setCurrentTime(mTime, (CardView)v));
        }

        void bind(Time time) {
            mTime = time;
            ((TextView)itemView.findViewById(R.id.item_time_text)).setText(mTime.toString());
        }
    }

    private class TimeAdapter extends RecyclerView.Adapter<TimeHolder> {
        private Report mReport;
        private Time mRecentlyDeletedTime;
        private int mRecentlyDeletedTimePosition;

        TimeAdapter(Report report) {
            mReport = report;
        }

        @Override
        public void onBindViewHolder(@NonNull TimeHolder holder, int position, @NonNull List<Object> payloads) {
            Time time = mReport.get(position);
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
            return mReport.size();
        }

        void setReport(Report report) {
            mReport = report;
        }

        void deleteTime(int position) {
            mRecentlyDeletedTime = mReport.remove(position);
            mRecentlyDeletedTimePosition = position;

            notifyItemRemoved(position);
            showUndoSnackbar();
        }

        private void showUndoSnackbar() {
            View view = Objects.requireNonNull(getActivity()).findViewById(R.id.constraint_layout);
            Snackbar snackbar = Snackbar.make(view, "Time Deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", v -> undoDelete());
            snackbar.show();
        }

        private void undoDelete() {
            mReport.add(mRecentlyDeletedTimePosition, mRecentlyDeletedTime);
            notifyItemInserted(mRecentlyDeletedTimePosition);
        }
    }

    class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private TimeAdapter mAdapter;

        SwipeToDeleteCallback(TimeAdapter adapter) {
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
