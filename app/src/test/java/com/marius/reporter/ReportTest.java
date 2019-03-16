package com.marius.reporter;

import com.marius.reporter.models.Report;
import com.marius.reporter.models.Time;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReportTest {

    @Test
    public void getTotalTime() {
        Report report = new Report();

        Time x = new Time();
        x.setSeconds(50);
        x.setMinutes(40);
        x.setHours(20);
        report.add(x);

        Time y = new Time();
        y.setSeconds(25);
        y.setMinutes(32);
        y.setHours(7);
        report.add(y);

        Time z = new Time();
        z.setSeconds(25);
        z.setMinutes(32);
        z.setHours(7);
        report.add(z);

        assertEquals("35:45:40", report.getTotalTime());
    }
}