package com.marius.reporter.utils;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.marius.reporter.R;

public class DayNightSwitchPreference extends SwitchPreferenceCompat {
    @SuppressWarnings("unused")
    private static final String TAG = "DayNightSwitchPreference";

    public DayNightSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DayNightSwitchPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
//        holder.itemView.findViewById(R.id.)

//        holder.itemView.setOnClickListener(this.mClickListener);
//        holder.itemView.setId(this.mViewId);
        TextView titleView = (TextView) holder.findViewById(R.id.title);
        if (titleView != null) {
            CharSequence title = this.getTitle();
            if (!TextUtils.isEmpty(title)) {
                titleView.setText(title);
                titleView.setVisibility(View.VISIBLE);
            } else {
                titleView.setVisibility(View.INVISIBLE);
            }
        }

        TextView summaryView = (TextView) holder.findViewById(R.id.summary);
        if (summaryView != null) {
            CharSequence summary = this.getSummary();
            if (!TextUtils.isEmpty(summary)) {
                summaryView.setText(summary);
                summaryView.setVisibility(View.VISIBLE);
            } else {
                summaryView.setVisibility(View.INVISIBLE);
            }
        }


    }
}
