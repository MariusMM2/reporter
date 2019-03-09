package com.marius.reporter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.*;
import android.widget.TextView;
import com.marius.reporter.R;
import com.marius.reporter.Report;
import com.marius.reporter.Settings;
import com.marius.reporter.database.ReportRepo;

import java.io.Serializable;
import java.util.List;

import static com.marius.reporter.activities.ReportPagerActivity.EXTRA_REPORT_ID;

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
        void onReportDeleted(Report report);
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

        mReportRecyclerView = v.findViewById(R.id.report_recycler_view);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mReportRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new ReportAdapter();
        mReportRecyclerView.setAdapter(mAdapter);

//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(mAdapter));
//        itemTouchHelper.attachToRecyclerView(mReportRecyclerView);
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
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.new_report:
                Report report = new Report();
                report.setGPSName(mSettings.gpsName);
                mCallbacks.onReportSelected(report);
                ReportRepo.getInstance(getActivity()).addReport(report);
                mAdapter.setReports(ReportRepo.getInstance(getActivity()).getReports());
                mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
            mTitleTextView.setText(mReport.getName());
            String address = mReport.getAddress();
            if (address.equals("")) {
                mAddressTextView.setVisibility(View.GONE);
            } else {
                mAddressTextView.setText(address);
            }


            mTimeTextView.setText(mReport.getTotalTime());

            if (mReport.getId().equals(getActivity().getIntent().getSerializableExtra(EXTRA_REPORT_ID))) {
                getActivity().getIntent().putExtra(EXTRA_REPORT_ID, (Serializable) null);
                onClick(null);
            }
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

        private void deleteReport(int position) {
            mRecentlyDeletedReport = mReports.remove(position);
            ReportRepo.getInstance(getActivity()).deleteReport(mRecentlyDeletedReport.getId());
            mCallbacks.onReportDeleted(mRecentlyDeletedReport);

            notifyItemRemoved(position);
            showUndoSnackbar();
        }

        private void showUndoSnackbar() {
            View view = getActivity().findViewById(R.id.fragment_container);
            Snackbar snackbar = Snackbar.make(view, "Report Deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", v -> undoDelete());
            snackbar.show();
        }

        private void undoDelete() {
            ReportRepo.getInstance(getActivity()).addReport(mRecentlyDeletedReport);
            mReports.add(mRecentlyDeletedReport);
            notifyItemInserted(mReports.indexOf(mRecentlyDeletedReport));
        }
    }

    class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private ReportAdapter mAdapter;

        SwipeToDeleteCallback(ReportAdapter adapter) {
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

            mAdapter.deleteReport(position);
        }
    }
}
