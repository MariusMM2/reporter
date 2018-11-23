package com.marius.reporter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Report implements Serializable {
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

    public UUID getId() {
        return mId;
    }

    public String getFlyerName() {
        return mFlyerName;
    }

    public void setFlyerName(String flyerName) {
        mFlyerName = flyerName;
    }

    public short getRemainingFlyers() {
        return mRemainingFlyers;
    }

    public void setRemainingFlyers(int remainingFlyers) {
        mRemainingFlyers = (short) remainingFlyers;
    }

    public String getGPSName() {
        return mGPSName;
    }

    public void setGPSName(String GPSName) {
        mGPSName = GPSName;
    }

    public boolean hasTimes() {
        return mTimes.isEmpty();
    }

    public void addTime() {
        mTimes.add(new Time());
    }

    public List<Time> getTimes() {
        return mTimes;
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

    public boolean isWithRemainingFlyers() {
        return mWithRemainingFlyers;
    }

    public void setWithRemainingFlyers(boolean withRemainingFlyers) {
        mWithRemainingFlyers = withRemainingFlyers;
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
            this.hours = (byte) hours;
        }

        public byte getMinutes() {
            return minutes;
        }

        public void setMinutes(int minutes) {
            this.minutes = (byte) minutes;
        }

        public byte getSeconds() {
            return seconds;
        }

        public void setSeconds(int seconds) {
            this.seconds = (byte) seconds;
        }

        @Override
        public String toString() {
            return String.format(Locale.UK,"%02d:%02d:%02d", hours, minutes, seconds);
        }
    }
}
