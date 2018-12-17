package com.marius.reporter.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
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
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import com.marius.reporter.R;
import com.marius.reporter.Report;
import com.marius.reporter.Report.Time;
import com.marius.reporter.Settings;
import com.marius.reporter.TimeEditor;
import com.marius.reporter.database.FlyerNameRepo;
import com.marius.reporter.database.ReportRepo;
import com.marius.reporter.utils.anim.ViewTranslator;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public class ReportFragment extends Fragment implements Report.Callbacks {
    @SuppressWarnings("unused")
    private static final String TAG = ReportFragment.class.getSimpleName();

    @SuppressWarnings("unused")
    private class Arg {
        private static final String REPORT_ID = "report_id";
    }

    private Settings mSettings;

    private Callbacks mCallbacks;

    private ViewGroup mMainLayout;
    private AutoCompleteTextView mFlyerNameField;
    private AppCompatCheckBox mQuantityLeftLabel;
    private EditText mQuantityLeftField;
    private EditText mGpsNameField;
    private RecyclerView mTimeListView;
    private CardView mTimeEditorView;
    private FloatingActionButton mAddTimeButton;
    private FloatingActionButton mSendReportButton;
    private FloatingActionButton mDebugDummyButton;
    private View.OnTouchListener mTimeEditorTouchListener;

    private TimeAdapter mTimeListAdapter;
    private ArrayAdapter<String> mFlyerNameArrayAdapter;

    private Report mReport;
    private TimeEditor mTimeEditor;

    private ReportCheckTimer mSendFABTimer;

    public interface Callbacks {
        void onReportUpdated(Report report);
    }

    public static ReportFragment newInstance(UUID reportId) {
        Bundle args = new Bundle();
        args.putSerializable(Arg.REPORT_ID, reportId);
        ReportFragment fragment = new ReportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(getActivity());

        setHasOptionsMenu(true);
        mTimeEditor = new TimeEditor();
        mTimeEditorTouchListener = (v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                // Notify touch outside listener if user tapped outside a given view
                if (mTimeEditor != null && mTimeEditorView != null
                        && mTimeEditorView.getVisibility() == View.VISIBLE) {
                    Rect viewRect = new Rect();
                    mTimeEditorView.getGlobalVisibleRect(viewRect);
                    if (!viewRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        ((OnTouchOutsideListener) mTimeEditor).onTouchOutsideView(mTimeEditorView, event);
                        ReportFragment.this.detachTime();
                    }
                }
                return true;
            }
            return false;
        };

        UUID reportId = (UUID) getArguments().getSerializable(Arg.REPORT_ID);
        mReport = ReportRepo.getInstance(getActivity()).getReport(reportId);
        mReport.setCallBacks(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);

        mFlyerNameField    = v.findViewById(R.id.flyer_name);
        mQuantityLeftLabel = v.findViewById(R.id.remaining_flyers_label);
        mQuantityLeftField = v.findViewById(R.id.remaining_flyers_field);
        mGpsNameField      = v.findViewById(R.id.gps_name);
        mTimeListView = v.findViewById(R.id.times_recycler_view);
        mTimeEditorView    = v.findViewById(R.id.time_editor_card);
        mAddTimeButton     = v.findViewById(R.id.add_time_button);
        mSendReportButton  = v.findViewById(R.id.send_report_button);
        mDebugDummyButton  = v.findViewById(R.id.debug_dummy_button);
        mTimeEditor.init(mTimeEditorView);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mMainLayout = (ViewGroup) view;
        mMainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                updateSendFAB(0);

                mTimeEditor.hideNow();
            }
        });

        mFlyerNameField   .addTextChangedListener(new TextWatcher() {
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
        mQuantityLeftLabel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mReport.setWithRemainingFlyers(isChecked);
            mQuantityLeftField.setEnabled(isChecked);
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
            }
        });
        mGpsNameField     .addTextChangedListener(new TextWatcher() {
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
        mAddTimeButton    .setOnClickListener(v1 -> {
            mReport.add(new Time());
            mTimeListAdapter.notifyItemInserted(mTimeListAdapter.getItemCount() - 1);
        });
        mSendReportButton .setOnClickListener(v12 -> {
            FlyerNameRepo.getInstance(getActivity()).addFlyerName(mReport.getFlyerName());

            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, getReportOutput());
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_subject));
            final Intent i2 = Intent.createChooser(i, getString(R.string.send_report));
            ViewTranslator.moveOffscreen(mSendReportButton, ViewTranslator.Direction.RIGHT, () -> startActivity(i2));
        });
        mDebugDummyButton .setOnClickListener(v13 -> {
            mReport.from(Report.dummy());
            updateUI();
        });

        mFlyerNameArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line);
        mFlyerNameField.setAdapter(mFlyerNameArrayAdapter);
        updateUIViews();

        mTimeListAdapter = new TimeAdapter(mReport);
        mTimeListView.setAdapter(mTimeListAdapter);
        mTimeListView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mTimeListView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                    if (childView == null) {
                        detachTime();
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(mTimeListAdapter));
        itemTouchHelper.attachToRecyclerView(mTimeListView);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSendFABTimer= new ReportCheckTimer();
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
                mReport.reset();
                mReport.setCallBacks(this);
                mReport.setGPSName(mSettings.gpsName);
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        reportChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSendFABTimer.cancel();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private void updateUI() {
        updateUIViews();

        mTimeListAdapter.setReport(mReport);
        mTimeListAdapter.notifyDataSetChanged();
        mFlyerNameArrayAdapter.clear();
        mFlyerNameArrayAdapter.addAll(FlyerNameRepo.getInstance(getActivity()).getFlyerNames());
        mFlyerNameArrayAdapter.notifyDataSetChanged();
    }

    private void updateUIViews() {
        mFlyerNameField.setText(mReport.getFlyerName());
        mQuantityLeftLabel.setChecked(mReport.isWithRemainingFlyers());
        mQuantityLeftField.setEnabled(mReport.isWithRemainingFlyers());
        mQuantityLeftField.setText(String.valueOf(mReport.getRemainingFlyers()));
        mGpsNameField.setText(mReport.getGPSName());
    }

    private void updateSendFAB() {
        updateSendFAB(-1);
    }

    private void updateSendFAB(int duration) {
        if (mReport.isReadyToSend()) {
            mSendReportButton.setClickable(true);
            if (duration == -1) {
                ViewTranslator.moveFromBehind(mSendReportButton, mAddTimeButton);
            } else {
                ViewTranslator.moveFromBehind(mSendReportButton, mAddTimeButton, duration);
            }
        } else {
            mSendReportButton.setClickable(false);
            if (duration == -1) {
                ViewTranslator.moveToBehind(mSendReportButton, mAddTimeButton);
            } else {
                ViewTranslator.moveToBehind(mSendReportButton, mAddTimeButton, duration);
            }
        }
    }

    @Override
    public void reportChanged() {
        ReportRepo.getInstance(getActivity()).updateReport(mReport);
        mCallbacks.onReportUpdated(mReport);
        if (mSendFABTimer != null) mSendFABTimer.onReportChanged();
    }

    private void detachTime() {
        Log.d(TAG, "listener detached");
        mMainLayout.setOnTouchListener(null);
        mTimeEditor.detachTime();
        mTimeEditor.hide();
    }

    private void attachTime(View v, Time time) {
        if (v == mTimeEditor.getCurrentTimeView()) return;
        detachTime();
        mTimeEditor.attachTime(time, (CardView) v);
        Log.d(TAG, "listener attached to " + time.toString());
        mMainLayout.setOnTouchListener(mTimeEditorTouchListener);
        mTimeEditor.show();

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

    private class TimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Time mTime;
        private TextView mTimeTextView;

        TimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_time, parent, false));
            itemView.setOnClickListener(this);
            mTimeTextView = itemView.findViewById(R.id.item_time_text);
        }

        void bind(Time time) {
            mTime = time;
            mTimeTextView.setText(mTime.toString());
            if (mTimeEditor.isShown()) {
                if (!mTimeEditor.getCurrentTime().equals(mTime)) {
                    onClick(itemView);
                }
            }
        }

        @Override
        public void onClick(View v) {
            attachTime(v, mTime);
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
            if (mRecentlyDeletedTime.equals(mTimeEditor.getCurrentTime())) {
                mTimeEditor.detachTime();
                mTimeEditor.hide();
            }

            notifyItemRemoved(position);
            showUndoSnackbar();
        }

        private void showUndoSnackbar() {
            View view = getActivity().findViewById(R.id.constraint_layout);
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

    private class ReportCheckTimer extends Timer {

        private volatile boolean mReportUpdated;

        private ReportCheckTimer() {
            super("SendButtonUpdater", true);
            Handler handler = new Handler();
            this.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mReportUpdated) {
                        handler.post(ReportFragment.this::updateSendFAB);
                        mReportUpdated = false;
                    }
                }
            }, 0, 500);
        }

        private void onReportChanged() {
            mReportUpdated = true;
        }
    }

    /**
     * Interface definition for a callback to be invoked when a touch event has occurred outside a formerly specified
     * view.
     */
    public interface OnTouchOutsideListener {

        /**
         * Called when a touch event has occurred outside a given view.
         *
         * @param view  The view that has not been touched.
         * @param event The MotionEvent object containing full information about the event.
         */
        void onTouchOutsideView(View view, MotionEvent event);
    }
}
