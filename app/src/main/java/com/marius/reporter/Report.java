package com.marius.reporter;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

import java.io.Serializable;
import java.util.*;

public class Report implements Serializable {
    private transient Callbacks mCallBacks;
    private final UUID mId;
    private String mFlyerName;
    private short mRemainingFlyers;
    private boolean mWithRemainingFlyers;
    private transient String mGPSName;
    private List<Time> mTimes;

    public Report(Callbacks callbacks) {
        this(UUID.randomUUID(), callbacks);
    }

    public Report(UUID id, Callbacks callbacks) {
        mCallBacks = callbacks;
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
        mCallBacks.reportChanged();
    }

    public short getRemainingFlyers() {
        return mRemainingFlyers;
    }

    public void setRemainingFlyers(int remainingFlyers) {
        mRemainingFlyers = (short) remainingFlyers;
        mCallBacks.reportChanged();

    }

    public String getGPSName() {
        return mGPSName;
    }

    public void setGPSName(String GPSName) {
        mGPSName = GPSName;
        mCallBacks.reportChanged();
    }

    public boolean isWithRemainingFlyers() {
        return mWithRemainingFlyers;
    }

    public void setWithRemainingFlyers(boolean withRemainingFlyers) {
        mWithRemainingFlyers = withRemainingFlyers;
        mCallBacks.reportChanged();
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

    @SuppressWarnings("SpellCheckingInspection")
    public static Report dummy(Callbacks callbacks) {
        Report report = new Report(callbacks);

        Random rand = new Random();
        Lorem lorem = LoremIpsum.getInstance();

        report.setFlyerName(lorem.getTitle(10));
        report.setGPSName(lorem.getName());
        report.setWithRemainingFlyers(rand.nextBoolean());
        report.setRemainingFlyers(rand.nextInt(1001));

        for (int i = 0; i < 20; i++)
            report.add(new Time(rand.nextInt(24), rand.nextInt(60), rand.nextInt(60)));

        return report;
    }

    //Times list delegates
    public int size() {
        return mTimes.size();
    }
    public boolean add(Time time) {
        boolean result = mTimes.add(time);
        mCallBacks.reportChanged();
        return result;
    }
    public void add(int index, Time element) {
        mTimes.add(index, element);
        mCallBacks.reportChanged();
    }
    public Time get(int index) {
        return mTimes.get(index);
    }
    public Time remove(int pos) {
        Time result = mTimes.remove(pos);
        mCallBacks.reportChanged();
        return result;
    }

    public static class Time implements Serializable {
        private byte hours;
        private byte minutes;
        private byte seconds;

        public Time() {
            this(0, 0, 0);
        }

        public Time(int hours, int minutes, int seconds) {
            setHours(hours);
            setMinutes(minutes);
            setSeconds(seconds);
        }

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
