package org.tachiyomi;

/*
 * Copyright (C) 2025 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import java.nio.charset.StandardCharsets;

public class NativeChannel {
    public static void call(String topic, String content) {
        System.out.println("NativeChannel: topic:" + topic + " content:"+content);
        call_utf8(stringToUtf8ByteArray(topic), stringToUtf8ByteArray(content));
    }

    static native void call_utf8(byte[] topicUtf8, byte[] contentUtf8);

    static byte[] stringToUtf8ByteArray(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
