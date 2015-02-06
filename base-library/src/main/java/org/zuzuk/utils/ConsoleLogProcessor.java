package org.zuzuk.utils;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by Gavriil Sitnikov on 06/02/2015.
 * Default LogProcessor that writes logs in logcat console
 */
public class ConsoleLogProcessor implements Lc.LogProcessor {

    @Override
    public void processLogMessage(int logLevel, String tag, String message) {
        switch (logLevel) {
            case Log.DEBUG:
                Log.d(tag, message);
                break;
            case Log.INFO:
                Log.i(tag, message);
                break;
            case Log.WARN:
                Log.w(tag, message);
                break;
            case Log.ERROR:
            case Log.ASSERT:
                Log.e(tag, message);
                break;
        }
    }

    @Override
    public void processLogMessage(int logLevel, String tag, String message, @NonNull Throwable ex) {
        switch (logLevel) {
            case Log.DEBUG:
                Log.d(tag, message, ex);
                break;
            case Log.INFO:
                Log.i(tag, message, ex);
                break;
            case Log.WARN:
                Log.w(tag, message, ex);
                break;
            case Log.ERROR:
            case Log.ASSERT:
                Log.e(tag, message, ex);
                break;
            default:
                throw new IllegalStateException("Unsupported log level: " + logLevel);
        }
    }
}
