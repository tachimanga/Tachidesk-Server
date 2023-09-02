package org.tachiyomi;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
 
public class NativeNet {
    public static String call(String json) {
        System.out.println("NativeNet:" + json);
        //call_utf8(stringToUtf8ByteArray(json));
        return "";
    }

    static native ByteBuffer call_utf8(byte[] jsonUtf8);

    static byte[] stringToUtf8ByteArray(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
