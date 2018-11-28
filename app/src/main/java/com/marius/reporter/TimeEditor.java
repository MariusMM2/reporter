package com.marius.reporter;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.marius.reporter.utils.anim.ViewElevator;

import java.util.Locale;

public class TimeEditor {
    private NumberPicker[] mPickers;
    private Report.Time mTime;

    private CardView mTimeHolderCard;

    private static int  HOUR   = 0,
                        MINUTE = 1,
                        SECOND = 2;


    public TimeEditor() {
        mTime = new Report.Time();
        mPickers = new NumberPicker[3];
    }

    public void init(View parent) {
        mPickers[HOUR]   = parent.findViewById(R.id.hour_picker);
        mPickers[MINUTE] = parent.findViewById(R.id.minute_picker);
        mPickers[SECOND] = parent.findViewById(R.id.second_picker);

        mPickers[HOUR]  .setMaxValue(23);
        mPickers[MINUTE].setMaxValue(59);
        mPickers[SECOND].setMaxValue(59);

        mPickers[HOUR].setOnValueChangedListener((picker, oldVal, newVal) -> {
            mTime.setHours(newVal);
            updateText();
        });
        mPickers[MINUTE].setOnValueChangedListener((picker, oldVal, newVal) -> {
            mTime.setMinutes(newVal);
            updateText();
        });
        mPickers[SECOND].setOnValueChangedListener((picker, oldVal, newVal) -> {
            mTime.setSeconds(newVal);
            updateText();
        });

        for (NumberPicker picker : mPickers) picker.setFormatter(TimeEditor::format);

        for (NumberPicker picker : mPickers) picker.setEnabled(false);
    }

    public void setCurrentTime(Report.Time time, CardView timeHolderCard) {
        if (mTimeHolderCard == timeHolderCard) return;

        ViewElevator.elevate(timeHolderCard, R.dimen.view_elevated).start();

        if (mTimeHolderCard != null)
            ViewElevator.elevate(mTimeHolderCard, R.dimen.view_normal).start();

        mTime = time;
        mPickers[HOUR].setValue(mTime.getHours());
        mPickers[MINUTE].setValue(mTime.getMinutes());
        mPickers[SECOND].setValue(mTime.getSeconds());

        for (NumberPicker picker : mPickers) if (!picker.isEnabled()) picker.setEnabled(true);

        mTimeHolderCard = timeHolderCard;
    }

    private static String format(int value) {
        return String.format(Locale.UK, "%02d", value);
    }

    private void updateText() {
        if (mTimeHolderCard != null)
            ((TextView) mTimeHolderCard.findViewById(R.id.item_time_text)).setText(mTime.toString());
    }
}
