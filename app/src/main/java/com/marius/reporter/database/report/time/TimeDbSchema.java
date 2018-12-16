package com.marius.reporter.database.report.time;

public class TimeDbSchema {
    public static final class TimeTable {
        public static final String NAME = "times";

        public static final class Cols {
            public static final String REPORT_UUID = "report_uuid";
            public static final String HOUR = "hour";
            public static final String MINUTE = "minute";
            public static final String SECOND = "second";
        }
    }
}
