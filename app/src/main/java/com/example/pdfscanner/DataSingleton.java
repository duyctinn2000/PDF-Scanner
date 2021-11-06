package com.example.pdfscanner;

import android.content.Context;

public class DataSingleton {
    private static DataSingleton dataSingleton;
    private Data data;

    public DataSingleton(Context context) {
        data = new Data();
    }

    public static DataSingleton get(Context context) {
        if (dataSingleton == null) {
            dataSingleton = new DataSingleton(context);
        }
        return dataSingleton;
    }

    public Data getData() {
        return data;
    }

    public static DataSingleton newDataSingleton(Context context) {
        dataSingleton = new DataSingleton(context);
        return dataSingleton;
    }
}
