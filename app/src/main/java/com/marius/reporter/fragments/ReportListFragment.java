package com.marius.reporter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.*;
import android.widget.TextView;
import com.marius.reporter.R;
import com.marius.reporter.Report;
import com.marius.reporter.ReportRepo;
import com.marius.reporter.utils.anim.ViewElevator;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Callbacks} interface
 * to handle interaction events.
 */
public class ReportListFragment extends Fragment {

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
        boolean isMasterDetail();
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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report_list, container, false);

        mReportRecyclerView = v.findViewById(R.id.report_recycler_view);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mReportRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(mAdapter));
        itemTouchHelper.attachToRecyclerView(mReportRecyclerView);
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
        switch (item.getItemId()) {
            case R.id.new_report:
                Report report = new Report();
                mCallbacks.onReportSelected(report);
                ReportRepo.getInstance(getActivity()).addReport(report);
                mAdapter.mReports.add(report);
                mAdapter.notifyItemInserted(mAdapter.mReports.indexOf(report));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateUI() {
        List<Report> reports = ReportRepo.getInstance(getActivity()).getReports();

        if (mAdapter == null) {
            mAdapter = new ReportAdapter(reports);
            mReportRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setReports(reports);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class ReportHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Report mReport;
        private TextView mTitleTextView;
        private ReportAdapter mAdapter;

        private ReportHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_report, parent, false));
            itemView.setOnClickListener(this);
            mTitleTextView = itemView.findViewById(R.id.report_title);
        }

        private void bind(Report report, ReportAdapter adapter) {
            mReport = report;
            mAdapter = adapter;
            mTitleTextView.setText(mReport.getFlyerName());
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onReportSelected(mReport);
            mAdapter.onHolderSelected(this);
        }
    }

    private class ReportAdapter extends RecyclerView.Adapter<ReportHolder> {
        private List<Report> mReports;
        private ReportHolder mSelectedHolderView;
        private Report mRecentlyDeletedReport;

        ReportAdapter(List<Report> reports) {
            mReports = reports;
        }

        @Override
        public void onBindViewHolder(@NonNull ReportHolder holder, int position, @NonNull List<Object> payloads) {
            Report report = mReports.get(position);
            holder.bind(report, this);
        }

        @NonNull
        @Override
        public ReportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ReportHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportHolder reportHolder, int i) {

        }

        @Override
        public int getItemCount() {
            return mReports.size();
        }

        private void onHolderSelected(ReportHolder selectedHolderView) {

            if (mCallbacks.isMasterDetail()) {
                if (mSelectedHolderView == null) {
                    mSelectedHolderView = selectedHolderView;
                    ViewElevator.elevate(mSelectedHolderView.itemView, R.dimen.card_elevation_high).start();
                } else if (mSelectedHolderView != selectedHolderView) {
                    ViewElevator.elevate(mSelectedHolderView.itemView, R.dimen.card_elevation_low).start();
                    mSelectedHolderView = selectedHolderView;
                    if (mSelectedHolderView != null) {
                        ViewElevator.elevate(mSelectedHolderView.itemView, R.dimen.card_elevation_high).start();
                    }
                }
            } else {
                mSelectedHolderView = selectedHolderView;
            }
        }

        private void setReports(List<Report> reports) {
            mReports = reports;
        }

        private void deleteReport(int position) {
            mRecentlyDeletedReport = mReports.remove(position);
            if (mRecentlyDeletedReport.equals(mSelectedHolderView.mReport)) {
                onHolderSelected(null);
            }
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
