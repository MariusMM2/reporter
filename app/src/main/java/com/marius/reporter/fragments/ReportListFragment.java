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
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import com.marius.reporter.R;
import com.marius.reporter.Report;
import com.marius.reporter.database.ReportRepo;
import com.marius.reporter.utils.anim.ViewElevator;

import java.io.Serializable;
import java.util.ArrayList;
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

    private RecyclerView mReportRecyclerView;
    private ReportAdapter mReportAdapter;
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

        mReportAdapter = new ReportAdapter();
        mReportRecyclerView.setAdapter(mReportAdapter);
        mReportAdapter.setReports(ReportRepo.getInstance(getActivity()).getReports());

        updateUI();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(mReportAdapter));
        itemTouchHelper.attachToRecyclerView(mReportRecyclerView);
    }

    @Override
    public void onStart() {
        super.onStart();
//        mReportAdapter.setReports(ReportRepo.getInstance(getActivity()).getReports());
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

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_report:
                Report report = new Report();
                Log.d(TAG, "created report with id '" + report.getId() + "'");
//                mCallbacks.onReportSelected(report);
                ReportRepo.getInstance(getActivity()).addReport(report);
                mReportAdapter.add(report);
                mReportAdapter.mOnItemCreated = true;
                mReportAdapter.onHolderRemoved();
                mReportAdapter.notifyItemInserted(mReportAdapter.indexOf(report));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateUI() {
        mReportAdapter.setReports(ReportRepo.getInstance(getActivity()).getReports());
        mReportAdapter.notifyDataSetChanged();
    }

    private class ReportHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ReportAdapter.ListReportItem mItem;
        private TextView mTitleTextView;
        private ReportAdapter mAdapter;

        private ReportHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_report, parent, false));
            itemView.setOnClickListener(this);
            mTitleTextView = itemView.findViewById(R.id.report_title);
        }

        private void bind(ReportAdapter.ListReportItem report, ReportAdapter adapter) {
            mItem = report;
            mAdapter = adapter;
            if (mItem.getId().equals(getActivity().getIntent().getSerializableExtra(EXTRA_REPORT_ID))) {
                getActivity().getIntent().putExtra(EXTRA_REPORT_ID, (Serializable) null);
                onClick(null);
            }
            if (mAdapter.mOnItemCreated) {
                if (mAdapter.mReports.indexOf(mItem) == mAdapter.mReports.size() - 1) {
                    mAdapter.mOnItemCreated = false;
                    onClick(null);
                }
            }
            updateUI();
        }

        private void updateUI() {
            mTitleTextView.setText(mItem.getFlyerName());
            ViewElevator.elevate(itemView, mItem.mSelected ? R.dimen.card_elevation_high : R.dimen.card_elevation_low).start();
        }

        @Override
        public void onClick(View v) {
            if (mCallbacks.isMasterDetail()) {
                for (ReportAdapter.ListReportItem listReportItem :
                        mReportAdapter.mReports) {
                    listReportItem.deselect();
                }
                mItem.select();
                updateUI();

                if (!this.mItem.equals(mAdapter.mSelectedReport)) {
                    mCallbacks.onReportSelected(mItem);
                    mAdapter.onHolderSelected(this);
                }
            } else {
                mCallbacks.onReportSelected(mItem);
                mAdapter.onHolderSelected(this);
            }
        }
    }

    private class ReportAdapter extends RecyclerView.Adapter<ReportHolder> {
        private List<ListReportItem> mReports;
        private ReportHolder mSelectedHolderView;
        private ListReportItem mSelectedReport;
        private ListReportItem mRecentlyDeletedReport;
        private boolean mOnItemCreated;

        ReportAdapter() {
            mReports = new ArrayList<>();
            mOnItemCreated = false;
        }

        @Override
        public void onBindViewHolder(@NonNull ReportHolder holder, int position, @NonNull List<Object> payloads) {
            holder.bind(mReports.get(position), this);
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
                if (selectedHolderView == null) {
                    onHolderRemoved();
                } else {
                    if (mSelectedHolderView == null) {
                        mSelectedHolderView = selectedHolderView;
                        mSelectedHolderView.mItem.select();
                    } else if (mSelectedHolderView != selectedHolderView) {
                        onHolderRemoved();
                        mSelectedHolderView = selectedHolderView;
                        mSelectedHolderView.mItem.select();
                        mSelectedHolderView.updateUI();
                    }
                }
            } else {
                mSelectedHolderView = selectedHolderView;
            }
            mSelectedReport = (mSelectedHolderView != null) ? mSelectedHolderView.mItem : null;
        }

        private void onHolderRemoved() {
            if (mSelectedHolderView != null) {
                mSelectedHolderView.mItem.deselect();
                mSelectedHolderView.updateUI();
//                ViewElevator.elevate(mSelectedHolderView.itemView, R.dimen.card_elevation_low).start();
                mSelectedHolderView = null;
            }
        }

        private void setReports(List<Report> reports) {
            mReports.clear();
            for (Report report : reports) {
                ListReportItem listReportItem = new ListReportItem(report);
                if (listReportItem.equals(mSelectedReport)) {
                    listReportItem.select();
                }
                mReports.add(listReportItem);
            }
        }

        private void deleteReport(int position) {
            mRecentlyDeletedReport = mReports.remove(position);
            if (mRecentlyDeletedReport.equals(mSelectedReport)) {
                onHolderSelected(null);
                mCallbacks.onReportDeleted(mRecentlyDeletedReport);
            }
            ReportRepo.getInstance(getActivity()).deleteReport(mRecentlyDeletedReport.getId());

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

        public void add(Report report) {
            mReports.add(new ReportAdapter.ListReportItem(report));
        }

        public int indexOf(Report report) {
            for (int i = 0; i < mReports.size(); i++) {
                if (mReports.get(i).getId().equals(report.getId())) {
                    return i;
                }
            }
            return -1;
        }

        private class ListReportItem extends Report {
            private boolean mSelected;

            public ListReportItem(Report report) {
                super(report);
                this.mSelected = false;
            }

            private void select() {
                mSelected = true;
            }

            private void deselect() {
                mSelected = false;
            }
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
