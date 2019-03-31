package com.marius.reporter.utils;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.AttributeSet;
import com.mahfa.dnswitch.DayNightSwitch;
import com.mahfa.dnswitch.DayNightSwitchAnimListener;
import com.marius.reporter.R;
import com.marius.reporter.Settings;

public class DayNightSwitchPreference extends SwitchPreferenceCompat {
    @SuppressWarnings("unused")
    private static final String TAG = "DayNightSwitchPreference";

    public DayNightSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.theme_switch);
    }

    public DayNightSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.findViewById(R.id.switch_item).setClickable(false);

        DayNightSwitch dayNightSwitch = (DayNightSwitch) holder.findViewById(R.id.switch_item);
        dayNightSwitch.setIsNight(Settings.getInstance(getContext()).isNightMode());
        dayNightSwitch.setAnimListener(new DayNightSwitchAnimListener() {
            @Override
            public void onAnimStart() {
                holder.itemView.setClickable(false);
            }

            @Override
            public void onAnimEnd() {
                persistBoolean(dayNightSwitch.isNight());
                Settings.getInstance(getContext()).refreshTheme();
            }

            @Override
            public void onAnimValueChanged(float v) {

            }
        });

        holder.itemView.setOnClickListener(v -> dayNightSwitch.toggle());
    }
}
