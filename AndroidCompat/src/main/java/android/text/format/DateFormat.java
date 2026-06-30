//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.text.format;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

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
        return skeleton;
    }

    public static java.text.DateFormat getTimeFormat(Context context) {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    }

    public static java.text.DateFormat getDateFormat(Context context) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public static java.text.DateFormat getLongDateFormat(Context context) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public static java.text.DateFormat getMediumDateFormat(Context context) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public static char[] getDateFormatOrder(Context context) {
        return new char[]{'y', 'M', 'd'};
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
