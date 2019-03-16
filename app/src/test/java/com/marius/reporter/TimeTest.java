package com.marius.reporter;

import com.marius.reporter.models.Time;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeTest {

    @Test
    public void add() {
        Time x = new Time();
        x.setSeconds(50);
        x.setMinutes(40);
        x.setHours(20);

        Time y = new Time();
        y.setSeconds(25);
        y.setMinutes(32);
        y.setHours(7);

        Time result = x.add(y);

        assertEquals("28:13:15", result.toString());
    }
}