package com.marius.reporter;

import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.marius.reporter.Report.Time;
import com.marius.reporter.fragments.ReportFragment;
import com.marius.reporter.utils.anim.ViewElevator;
import com.marius.reporter.utils.anim.ViewTranslator;
import com.marius.reporter.utils.anim.ViewTranslator.Direction;

import java.util.Locale;

public class TimeEditor implements ReportFragment.OnTouchOutsideListener {
    private static final int HOUR   = 0,
            MINUTE = 1,
            SECOND = 2;

    private NumberPicker[] mPickers;
    private View mContainer;
    private View mTimeHolderCard;
    private Time mTime;
    private static final Direction SLIDE_DIR = Direction.DOWN;
    private boolean mShown;

    public TimeEditor() {
        mTime = new Time();
        mPickers = new NumberPicker[3];
        mShown = false;
    }

    public void init(View container) {
        mContainer = container;

        mPickers[HOUR]   = mContainer.findViewById(R.id.hour_picker);
        mPickers[MINUTE] = mContainer.findViewById(R.id.minute_picker);
        mPickers[SECOND] = mContainer.findViewById(R.id.second_picker);

        mPickers[HOUR]  .setMaxValue(23);
        mPickers[MINUTE].setMaxValue(59);
        mPickers[SECOND].setMaxValue(59);

        mPickers[HOUR]  .setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (mTime != null) mTime.setHours(newVal);
            updateText();
        });
        mPickers[MINUTE].setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (mTime != null) mTime.setMinutes(newVal);
            updateText();
        });
        mPickers[SECOND].setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (mTime != null) mTime.setSeconds(newVal);
            updateText();
        });

        for (NumberPicker n : mPickers) n.setFormatter(TimeEditor::format);
        for (NumberPicker n : mPickers) n.setEnabled(false);
    }

    public void attachTime(Time time, CardView timeHolderCard) {
        if (mTimeHolderCard == timeHolderCard) return;

        ViewElevator.elevate(timeHolderCard, R.dimen.card_elevation_high).start();

        mTimeHolderCard = timeHolderCard;

        mTime = time;
        mPickers[HOUR]  .setValue(mTime.getHours());
        mPickers[MINUTE].setValue(mTime.getMinutes());
        mPickers[SECOND].setValue(mTime.getSeconds());

        for (NumberPicker n : mPickers) n.setEnabled(true);
    }

    public void detachTime() {
        if (mTimeHolderCard != null) {
            ViewElevator.elevate(mTimeHolderCard, R.dimen.card_elevation_low).start();
            mTimeHolderCard = null;
        }

        mTime = null;

        for (NumberPicker n : mPickers) n.setEnabled(false);
    }

    public void show() {
        mShown = true;
        ViewTranslator.slideInAndShow(mContainer, SLIDE_DIR);
    }

    public void hide() {
        mShown = false;
        ViewTranslator.slideOutAndHide(mContainer, SLIDE_DIR);
    }

    public void hideNow() {
        ViewTranslator.slideOutAndHide(mContainer, SLIDE_DIR, 0);
    }

    private static String format(int value) {
        return String.format(Locale.UK, "%02d", value);
    }

    private void updateText() {
        if (mTimeHolderCard != null)
            ((TextView) mTimeHolderCard.findViewById(R.id.item_time_text)).setText(mTime.toString());
    }

    public boolean isShown() {
        return mShown;
    }

    public Time getCurrentTime() {
        return mTime;
    }

    public View getCurrentTimeView() {
        return mTimeHolderCard;
    }

    @Override
    public void onTouchOutsideView(View view, MotionEvent event) {
        detachTime();
    }
}