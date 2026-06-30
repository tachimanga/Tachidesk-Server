package android.text.format;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.content.Context;

import java.text.DecimalFormat;

/**
 * Custom reimplementation of some of the methods used in Android.
 */
public class Formatter {
    private Formatter() {
    }

    public static String formatFileSize(Context context, long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB", "PB", "EB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String formatShortFileSize(Context context, long sizeBytes) {
        return formatFileSize(context, sizeBytes);
    }

    /** @deprecated */
    @Deprecated
    public static String formatIpAddress(int ipv4Address) {
        return (ipv4Address & 0xFF) + "." + ((ipv4Address >> 8) & 0xFF) + "." + ((ipv4Address >> 16) & 0xFF) + "." + ((ipv4Address >> 24) & 0xFF);
    }
}
