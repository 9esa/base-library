package org.zuzuk.utils;

import android.util.Log;

/**
 * Created by Gavriil Sitnikov on 04/09/2014.
 * Simple logger using caller class name as tag
 */
public class Ln {

    private static String getTag() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return stackTraceElements[4].getClassName();
    }

    /* Verbose level log */
    public static void v(String message) {
        //TODO: disable logs
        Log.v(getTag(), message);
    }

    /* Debug level log */
    public static void d(String message) {
        //TODO: disable logs
        Log.d(getTag(), message);
    }

    /* Info level log */
    public static void i(String message) {
        Log.i(getTag(), message);
    }

    /* Warning level log */
    public static void w(String message) {
        Log.w(getTag(), message);
    }

    /* Error level log */
    public static void e(String message) {
        Log.e(getTag(), message);
    }
}
