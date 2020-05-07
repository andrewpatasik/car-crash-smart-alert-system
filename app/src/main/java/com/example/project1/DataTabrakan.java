package com.example.project1;

import android.provider.BaseColumns;

public class DataTabrakan {

    private DataTabrakan(){}

    public static final class TabrakanEntry implements BaseColumns{
        public static final String TABLE_NAME = "tabrakan";         //nama tabel
        public static final String COLUMN_X = "sumbu_x";         //nama kolom pertama
        public static final String COLUMN_Y = "sumbu_y";
        public static final String COLUMN_LAT = "latitude";
        public static final String COLUMN_LONG = "longitude";
        public static final String COLUMN_TIMESTAMP = "timestamp";


    }
}
