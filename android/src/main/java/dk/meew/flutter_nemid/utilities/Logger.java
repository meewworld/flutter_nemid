package dk.meew.flutter_nemid.utilities;

import android.util.Log;

import dk.meew.flutter_nemid.BuildConfig;

public class Logger {
    enum Level {
        NONE,
        ERROR,
        WARN,
        INFO,
        DEBUG
    }

    static Level userDefinedLevel = null;

    static boolean debugEnabled;
    static boolean infoEnabled;
    static boolean warnEnabled;
    static boolean errorEnabled;

    static {
        if (userDefinedLevel != null) {
            setLevel(userDefinedLevel);
        } else {
            setLevel(BuildConfig.DEBUG ? Level.DEBUG : Level.NONE);
        }
    }

    static void setLevel(Level level) {
        debugEnabled = isLevelAllowed(level, Level.DEBUG);
        infoEnabled = isLevelAllowed(level, Level.INFO);
        warnEnabled = isLevelAllowed(level, Level.WARN);
        errorEnabled = isLevelAllowed(level, Level.ERROR);
    }

    private static boolean isLevelAllowed(Level levelThreshold, Level level) {
        return level.ordinal() <= levelThreshold.ordinal();
    }

    public static void d(String tag, String message) {
        if (debugEnabled) {
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (infoEnabled) {
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (warnEnabled) {
            Log.w(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (errorEnabled) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (errorEnabled) {
            if (throwable != null) {
                Log.e(tag, message, throwable);
            } else {
                Log.e(tag, message);
            }
        }
    }
}
