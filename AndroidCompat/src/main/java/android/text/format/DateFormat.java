//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.text.format;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormat {
    public DateFormat() {

    }

    public static boolean is24HourFormat(Context context) {
        return true;
    }

    public static String getBestDateTimePattern(Locale locale, String skeleton) {
        throw new RuntimeException("Stub!");
    }

    public static java.text.DateFormat getTimeFormat(Context context) {
        throw new RuntimeException("Stub!");
    }

    public static java.text.DateFormat getDateFormat(Context context) {
        throw new RuntimeException("Stub!");
    }

    public static java.text.DateFormat getLongDateFormat(Context context) {
        throw new RuntimeException("Stub!");
    }

    public static java.text.DateFormat getMediumDateFormat(Context context) {
        throw new RuntimeException("Stub!");
    }

    public static char[] getDateFormatOrder(Context context) {
        throw new RuntimeException("Stub!");
    }

    public static CharSequence format(CharSequence inFormat, long inTimeInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat(inFormat.toString());
        return formatter.format(new Date(inTimeInMillis));
    }

    public static CharSequence format(CharSequence inFormat, Date inDate) {
        SimpleDateFormat formatter = new SimpleDateFormat(inFormat.toString());
        return formatter.format(inDate);
    }

    public static CharSequence format(CharSequence inFormat, Calendar inDate) {
        SimpleDateFormat formatter = new SimpleDateFormat(inFormat.toString());
        return formatter.format(inDate.getTime());
    }
}
