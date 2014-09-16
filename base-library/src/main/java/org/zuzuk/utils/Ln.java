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
        if (Log.isLoggable("Ln", Log.VERBOSE)) {
            Log.v(getTag(), message);
        }
    }

    /* Debug level log */
    public static void d(String message) {
        if (Log.isLoggable("Ln", Log.DEBUG)) {
            Log.d(getTag(), message);
        }
    }

    /* Info level log */
    public static void i(String message) {
        if (Log.isLoggable("Ln", Log.INFO)) {
            Log.i(getTag(), message);
        }
    }

    /* Warning level log */
    public static void w(String message) {
        if (Log.isLoggable("Ln", Log.WARN)) {
            Log.w(getTag(), message);
        }
    }

    /* Error level log */
    public static void e(String message) {
        if (Log.isLoggable("Ln", Log.ERROR)) {
            Log.e(getTag(), message);
        }
    }
}
