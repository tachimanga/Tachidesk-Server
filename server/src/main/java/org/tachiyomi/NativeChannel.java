package org.tachiyomi;

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
