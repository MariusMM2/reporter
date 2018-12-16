package com.marius.reporter;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

import java.io.Serializable;
import java.util.*;

public class Report implements Serializable {
    private transient Callbacks mCallBacks;
    private UUID mId;
    private String mFlyerName;
    private short mRemainingFlyers;
    private boolean mWithRemainingFlyers;
    private transient String mGPSName;
    private List<Time> mTimes;

    public Report() {
        this(UUID.randomUUID());
    }

    public Report(UUID id) {
        mId = id;
        mFlyerName = "";
        mRemainingFlyers = 0;
        mWithRemainingFlyers = true;
        mGPSName = "";
        mTimes = new ArrayList<>();
    }

    public void setCallBacks(Callbacks callBacks) {
        mCallBacks = callBacks;
    }

    public UUID getId() {
        return mId;
    }

    public String getFlyerName() {
        return mFlyerName;
    }

    public void setFlyerName(String flyerName) {
        mFlyerName = flyerName;
        notifyChange();
    }

    public short getRemainingFlyers() {
        return mRemainingFlyers;
    }

    public void setRemainingFlyers(int remainingFlyers) {
        mRemainingFlyers = (short) remainingFlyers;
        notifyChange();
    }

    public String getGPSName() {
        return mGPSName;
    }

    public void setGPSName(String GPSName) {
        mGPSName = GPSName;
        notifyChange();
    }

    public boolean isWithRemainingFlyers() {
        return mWithRemainingFlyers;
    }

    public void setWithRemainingFlyers(boolean withRemainingFlyers) {
        mWithRemainingFlyers = withRemainingFlyers;
        notifyChange();
    }

    public boolean isReadyToSend() {
        boolean flyerName = !mFlyerName.isEmpty();
        boolean remainingFlyer = !mWithRemainingFlyers || mRemainingFlyers != 0;
        boolean gpsName = !mGPSName.isEmpty();
        boolean times = !mTimes.isEmpty();
        return flyerName && remainingFlyer && gpsName && times;
    }

    public String getTimesString() {
        StringBuilder timesBuilder = new StringBuilder();
        if (mTimes.size() > 0) {
            timesBuilder.append(mTimes.get(0));
            if (mTimes.size() > 1) {
                for (int i = 1; i < mTimes.size(); i++) {
                    timesBuilder.append(", ");
                    timesBuilder.append(mTimes.get(i));
                }
            }
        }
        return timesBuilder.toString();
    }

    public void reset() {
        mFlyerName = "";
        mRemainingFlyers = 0;
        mWithRemainingFlyers = true;
        mTimes.clear();
        notifyChange();
    }

    public void from(Report newReport) {
        this.mFlyerName = newReport.mFlyerName;
        this.mGPSName = newReport.mGPSName;
        this.mWithRemainingFlyers = newReport.mWithRemainingFlyers;
        this.mRemainingFlyers = newReport.mRemainingFlyers;
        this.mTimes = newReport.mTimes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return mId.equals(report.mId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static Report dummy() {
        Report report = new Report();

        Random rand = new Random();
        Lorem lorem = LoremIpsum.getInstance();

        report.mFlyerName = lorem.getTitle(10);
        report.mGPSName = lorem.getName();
        report.mWithRemainingFlyers = rand.nextBoolean();
        report.mRemainingFlyers = (short) rand.nextInt(1001);

        for (int i = 0; i < 20; i++) {
            Time time = new Time();
            time.setHours(rand.nextInt(24));
            time.setMinutes(rand.nextInt(60));
            time.setSeconds(rand.nextInt(60));
            report.mTimes.add(time);
        }

        return report;
    }

    //Times list delegates
    public int size() {
        return mTimes.size();
    }
    public boolean add(Time time) {
        boolean result = mTimes.add(time);
        notifyChange();
        return result;
    }
    public void add(int index, Time element) {
        mTimes.add(index, element);
        notifyChange();
    }
    public Time get(int index) {
        return mTimes.get(index);
    }

    public Time remove(int pos) {
        Time result = mTimes.remove(pos);
        notifyChange();
        return result;
    }

    private void notifyChange() {
        if (mCallBacks != null) {
            mCallBacks.reportChanged();
        }
    }

    public static class Time implements Serializable {
        private byte hours;
        private byte minutes;
        private byte seconds;

        public byte getHours() {
            return hours;
        }

        public void setHours(int hours) {
            this.hours = (byte) Math.min(Math.max(hours, 0), 23);
        }

        public byte getMinutes() {
            return minutes;
        }

        public void setMinutes(int minutes) {
            this.minutes = (byte) Math.min(Math.max(minutes, 0), 59);
        }

        public byte getSeconds() {
            return seconds;
        }

        public void setSeconds(int seconds) {
            this.seconds = (byte) Math.min(Math.max(seconds, 0), 59);
        }

        @Override
        public String toString() {
            return String.format(Locale.UK,"%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    public interface Callbacks {
        void reportChanged();
    }
}
