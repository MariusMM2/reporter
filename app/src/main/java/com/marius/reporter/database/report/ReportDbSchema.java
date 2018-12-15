package com.marius.reporter.database.report;

public class ReportDbSchema {
    public static final class ReportTable {
        public static final String NAME = "reports";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String FLYER_NAME = "flyer_name";
            public static final String REMAINING_FLYERS = "remaining_flyers";
            public static final String WITH_REMAINING_FLYERS = "with_remaining_flyes";
            public static final String GPS_NAME = "gps_name";
        }
    }
}
