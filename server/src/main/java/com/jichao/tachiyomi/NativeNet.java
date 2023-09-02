package com.jichao.tachiyomi;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author mc
 */
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
