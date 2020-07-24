package dk.meew.flutter_nemid.utilities;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class DateUtil {
    private static final DateFormat TIME_FORMATTER_BUILD_DATE = new SimpleDateFormat("d/M/yy HH:mm:ss");

    public static String buildDateToLocalFormat(String buildDate) {
        if (TextUtils.isEmpty(buildDate)) {
            return null;
        } else {
            long secondsSince1970 = Long.parseLong(buildDate);
            long msSince1970 = secondsSince1970 * 1000;
            return TIME_FORMATTER_BUILD_DATE.format(new Date(msSince1970));
        }
    }

}
