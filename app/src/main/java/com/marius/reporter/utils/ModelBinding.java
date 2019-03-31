package com.marius.reporter.utils;

import android.view.View;
import android.widget.TextView;
import com.marius.reporter.R;
import com.marius.reporter.models.Report;

/**
 * Utility class for binding a model to its presentable view
 */
public class ModelBinding {
    // Binds an item to a container
    public static void bindReport(Report report, View container) {
        TextView titleView = container.findViewById(R.id.report_title);
        TextView addressView = container.findViewById(R.id.report_address);
        TextView timeView = container.findViewById(R.id.time_text);
        bindReport(report, titleView, addressView, timeView);
    }

    // Binds an item to specific elements of a container,
    // Used for the ViewHolder pattern
    public static void bindReport(Report report, TextView titleView, TextView addressView, TextView timeView) {
        titleView.setText(report.getName());

        String address = report.getAddress();
        if (address.equals("")) {
            addressView.setVisibility(View.GONE);
        } else {
            addressView.setVisibility(View.VISIBLE);
            addressView.setText(address);
        }

        timeView.setText(report.getTotalTime());
    }
}
