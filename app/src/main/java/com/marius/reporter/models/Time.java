package com.marius.reporter.models;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class Time {
    private UUID mId;
    private byte hours;
    private byte minutes;
    private byte seconds;

    public Time() {
        this(UUID.randomUUID());
    }

    public Time(UUID id) {
        this.mId = id;
    }

    public UUID getId() {
        return mId;
    }

    public byte getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = (byte) Math.max(hours, 0);
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

    public Time add(Time that) {
        Time result = new Time();

        int resultSeconds = (int) this.seconds + (int) that.seconds;
        result.setSeconds(resultSeconds % 60);
        int minutesCarryOver = (resultSeconds >= 60) ? 1 : 0;

        int resultMinutes = (int) this.minutes + (int) that.minutes + minutesCarryOver;
        result.setMinutes(resultMinutes % 60);
        int hoursCarryOver = (resultMinutes >= 60) ? 1 : 0;


        int resultHours = (int) this.hours + (int) that.hours + hoursCarryOver;
        result.setHours(resultHours);

        return result;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return hours >= 0 && hours < 99 ? String.format(Locale.UK, "%02d:%02d:%02d", hours, minutes, seconds)
                : "99:99:99";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Time time = (Time) o;
        return Objects.equals(mId, time.mId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId);
    }
}
