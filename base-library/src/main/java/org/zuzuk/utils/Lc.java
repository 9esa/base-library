package org.zuzuk.utils;

import android.text.TextUtils;
import android.util.Log;

import org.zuzuk.InitializationHelper;

import java.text.SimpleDateFormat;
import java.util.Locale;

import roboguice.util.temp.Ln;

public class Lc {
    private static int LogLevel;
    private static LogProcessor LogProcessor;

    private static final ThreadLocal<SimpleDateFormat> DateTimeFormatter =
            new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("HH:mm:ss.SSS", new Locale("ru"));
                }
            };

    /* Logging initialization */
    public static void initialize(int logLevel) {
        initialize(logLevel, logLevel, InitializationHelper.createDefaultLogProcessor());
    }

    /* Logging initialization with different log level for Robospice*/
    public static void initialize(int logLevel, int robospiceLogLevel) {
        initialize(logLevel, robospiceLogLevel, InitializationHelper.createDefaultLogProcessor());
    }

    /* Logging initialization with different log level for Robospice and custom log processor */
    public static void initialize(int logLevel, int robospiceLogLevel, LogProcessor logProcessor) {
        LogLevel = logLevel;
        LogProcessor = logProcessor;
        Ln.getConfig().setLoggingLevel(robospiceLogLevel);
        Ln.setPrint(new Ln.Print() {
            @Override
            public int println(int priority, String msg) {
                logMessage(priority, msg, 1);
                return 0;
            }
        });
        d(String.format("Configuring Logging, minimum log level is %s", Ln.logLevelToString(logLevel)));
    }

    /* Debug level log */
    public static void d(String message) {
        logMessage(Log.DEBUG, message);
    }

    /* Info level log */
    public static void i(String message) {
        logMessage(Log.INFO, message);
    }

    /* Warning level log */
    public static void w(String message) {
        logMessage(Log.WARN, message);
    }

    /* Error level log */
    public static void e(String message) {
        logMessage(Log.ERROR, message);
    }

    private static void logMessage(int priority, String message, int stackTraceAdditionalDepth) {
        if (priority >= LogLevel) {
            StackTraceElement trace = Thread.currentThread().getStackTrace()[5 + stackTraceAdditionalDepth];
            String tag = trace.getFileName() + ":" + trace.getLineNumber();
            String messageExtended = String.format("%s %s %s",
                    DateTimeFormatter.get().format(System.currentTimeMillis()),
                    Thread.currentThread().getName(), message);

            LogProcessor.processLogMessage(priority, tag, messageExtended);
        }
    }

    private static void logMessage(int priority, String message) {
        logMessage(priority, message, 0);
    }

    /* Prints stack trace in log with DEBUG level */
    public static void printStackTrace(String tag) {
        if (LogLevel <= Log.DEBUG) {
            Log.d(tag, TextUtils.join("\n", Thread.currentThread().getStackTrace()));
        }
    }

    public interface LogProcessor {

        /* Processes log message (e.g. log it in Console or log it in Crashlytics) */
        void processLogMessage(int logLevel, String tag, String message);
    }
}
