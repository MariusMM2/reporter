package com.marius.reporter.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import com.marius.reporter.R;
import com.marius.reporter.Settings;
import com.marius.reporter.database.ReportRepo;
import com.marius.reporter.models.Report;
import com.marius.reporter.utils.ModelBinding;

import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Callbacks} interface
 * to handle interaction events.
 */
public class ReportListFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = ReportListFragment.class.getSimpleName();

    private Settings mSettings;

    private FloatingActionButton mNewReportFAB;
    private RecyclerView mReportRecyclerView;
    private ReportAdapter mAdapter;
    private Callbacks mCallbacks;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface Callbacks {
        void onReportSelected(Report report);

        boolean hasReportQueued();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callbacks) {
            mCallbacks = (Callbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Callbacks");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(getActivity());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report_list, container, false);

        mNewReportFAB = v.findViewById(R.id.new_report);
        mReportRecyclerView = v.findViewById(R.id.report_recycler_view);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mNewReportFAB.setOnClickListener(v -> newReport());

        mReportRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new ReportAdapter();
        mReportRecyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new SwipeToDeleteCallback(mAdapter)).attachToRecyclerView(mReportRecyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_report_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SwitchStatementWithTooFewBranches
        switch (id) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void newReport() {
        if (!mCallbacks.hasReportQueued()) {
            Report report = new Report();
            report.setGPSName(mSettings.gpsName);
            mCallbacks.onReportSelected(report);
            mAdapter.addReport(report);
        }
    }

    public void updateUI() {
        List<Report> reports = ReportRepo.getInstance(getActivity()).getReports();

        mAdapter.setReports(reports);
        mAdapter.notifyDataSetChanged();
    }

    private class ReportHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Report mReport;
        private TextView mTitleTextView;
        private TextView mAddressTextView;
        private TextView mTimeTextView;

        private ReportHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_report, parent, false));
            itemView.setOnClickListener(this);
            mTitleTextView = itemView.findViewById(R.id.report_title);
            mAddressTextView = itemView.findViewById(R.id.report_address);
            mTimeTextView = itemView.findViewById(R.id.time_text);

            CardView timeCard = itemView.findViewById(R.id.time_card);
            timeCard.setClickable(false);
            timeCard.setForeground(null);
        }

        private void bind(Report report) {
            mReport = report;

            ModelBinding.bindReport(report, mTitleTextView, mAddressTextView, mTimeTextView);
        }

        public Report getReport() {
            return mReport;
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onReportSelected(mReport);
        }
    }

    private class ReportAdapter extends RecyclerView.Adapter<ReportHolder> {
        private List<Report> mReports;
        private Report mRecentlyDeletedReport;

        @Override
        public void onBindViewHolder(@NonNull ReportHolder reportHolder, int position, @NonNull List<Object> payloads) {
            Report report = mReports.get(position);
            reportHolder.bind(report);
        }

        @NonNull
        @Override
        public ReportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ReportHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportHolder reportHolder, int position) {

        }

        @Override
        public int getItemCount() {
            return mReports.size();
        }

        private void setReports(List<Report> reports) {
            mReports = reports;
        }

        public void addReport(Report report) {
            ReportRepo.getInstance(getActivity()).addReport(report);
            mReports.add(report);

            notifyItemInserted(mAdapter.getItemCount() - 1);
        }

        private void deleteReport(int position) {
            mRecentlyDeletedReport = mReports.remove(position);
            ReportRepo.getInstance(getActivity()).deleteReport(mRecentlyDeletedReport);

            notifyItemRemoved(position);
            showUndoSnackbar();
        }

        private void showUndoSnackbar() {
            View view = Objects.requireNonNull(getActivity()).findViewById(R.id.report_recycler_view);
            Snackbar snackbar = Snackbar.make(view, "Report Deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", v -> addReport(mRecentlyDeletedReport));
            snackbar.show();
        }
    }

    @SuppressWarnings("unused")
    class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private ReportAdapter mAdapter;
        private Drawable icon;
        private final ColorDrawable background;

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
//            int backgroundCornerOffset = 20;
            int backgroundCornerOffset = getResources().getDimensionPixelSize(R.dimen.card_corner_radius) * 2;

            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            if (dX > 0) { // Swiping to the right
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = iconLeft + icon.getIntrinsicWidth();
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
            } else if (dX < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
            } else { // view is unSwiped
                background.setBounds(0, 0, 0, 0);
            }

            background.draw(c);
            icon.draw(c);
        }

        @Override
        public float getSwipeEscapeVelocity(float defaultValue) {
            return defaultValue * 1.5f;
        }

        SwipeToDeleteCallback(ReportAdapter adapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            mAdapter = adapter;

            icon = ContextCompat.getDrawable(getContext(),
                    R.drawable.ic_delete_white);
            background = new ColorDrawable(Color.RED);

        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            int position = viewHolder.getAdapterPosition();

            View dialogReportItemView = View.inflate(getContext(), R.layout.list_item_report, null);

            TextView titleTextView = dialogReportItemView.findViewById(R.id.report_title);
            TextView addressTextView = dialogReportItemView.findViewById(R.id.report_address);
            TextView timeTextView = dialogReportItemView.findViewById(R.id.time_text);

            CardView timeCard = dialogReportItemView.findViewById(R.id.time_card);
            timeCard.setClickable(false);
            timeCard.setForeground(null);

            Report report = ((ReportHolder) viewHolder).getReport();

            ModelBinding.bindReport(report, titleTextView, addressTextView, timeTextView);

            Log.d(TAG, "Report Swiped");

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Delete report")
                    .setMessage("Are you sure you want to delete this report?")
                    .setPositiveButton(android.R.string.yes, (dialog1, which) -> mAdapter.deleteReport(position))
                    .setNegativeButton(android.R.string.no, (dialog14, which) -> dialog14.cancel())
                    .setOnCancelListener(dialog12 -> mAdapter.notifyItemChanged(position))
                    .setView(dialogReportItemView).show();
        }
    }
}
