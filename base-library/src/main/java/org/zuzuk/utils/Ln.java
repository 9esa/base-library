package org.zuzuk.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Gavriil Sitnikov on 04/09/2014.
 * Simple logger using caller class name as tag
 */
public class Ln {
    private static int LogLevel = Log.ERROR;

    /* Sets log level */
    public static void setLogLevel(int logLevel) {
        LogLevel = logLevel;
    }

    private static String getTag() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return stackTraceElements[4].getClassName();
    }

    /* Verbose level log */
    public static void v(String message) {
        if (LogLevel <= Log.VERBOSE) {
            Log.v(getTag(), message);
        }
    }

    /* Debug level log */
    public static void d(String message) {
        if (LogLevel <= Log.DEBUG) {
            Log.d(getTag(), message);
        }
    }

    /* Info level log */
    public static void i(String message) {
        if (LogLevel <= Log.INFO) {
            Log.i(getTag(), message);
        }
    }

    /* Warning level log */
    public static void w(String message) {
        if (LogLevel <= Log.WARN) {
            Log.w(getTag(), message);
        }
    }

    /* Error level log */
    public static void e(String message) {
        if (LogLevel <= Log.ERROR) {
            Log.e(getTag(), message);
        }
    }

    /* Prints stack trace in log with DEBUG level */
    public static void printStackTrace(String tag) {
        if (LogLevel <= Log.DEBUG) {
            Log.d(tag, TextUtils.join("\n", Thread.currentThread().getStackTrace()));
        }
    }
}
