package com.marius.reporter.models;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

import java.util.*;

public class Report {
    private Callbacks mCallBacks;
    private UUID mId;
    private String mFlyerName;
    private short mRemainingFlyers;
    private boolean mWithRemainingFlyers;
    private String mGPSName;
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

    public String getTotalTime() {

        Time total;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            total = Arrays.stream(getTimes()).reduce(new Time(), Time::add);
        } else {
            total = new Time();
            for (Time time : getTimes()) {
                total.add(time);
            }
        }

        return total.toString();
    }

    public String getTimesString() {
//        StringBuilder timesBuilder = new StringBuilder();
//        if (mTimes.size() > 0) {
//            timesBuilder.append(mTimes.get(0));
//            if (mTimes.size() > 1) {
//                for (int i = 1; i < mTimes.size(); i++) {
//                    timesBuilder.append(", ");
//                    timesBuilder.append(mTimes.get(i));
//                }
//            }
//        }
//        return timesBuilder.toString();
        StringBuilder timesBuilder = new StringBuilder();
        if (mTimes.size() > 0) {
            timesBuilder.append(mTimes.get(mTimes.size() - 1));
            if (mTimes.size() > 1) {
                for (int i = mTimes.size() - 2; i >= 0; i--) {
                    timesBuilder.append(", ");
                    timesBuilder.append(mTimes.get(i));
                }
            }
        }
        return timesBuilder.toString();
    }

    public String getName() {
        final int commaIndex = mFlyerName.indexOf(',');
        if (commaIndex != -1) {
            return mFlyerName.substring(0, commaIndex);
        } else {
            return getFlyerName();
        }
    }

    public String getAddress() {
        if (mFlyerName.indexOf(',') != -1) {
            return mFlyerName.substring(mFlyerName.indexOf(',') + 1).trim();
        } else {
            return "";
        }
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
        notifyChange();
    }

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

    @Override
    public String toString() {
        return String.format("'%s' (%s)", getName(), getId());
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

    public Time[] getTimes() {
        return mTimes.toArray(new Time[]{});
    }

    public interface Callbacks {
        void reportChanged();
    }
}
