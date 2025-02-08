package org.tachiyomi;

import io.javalin.plugin.json.JsonMapper;
import okio.Buffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
 
public class NativeNet {
    public static Resp call(Req req, Buffer buffer, JsonMapper jsonMapper) {
        String json = jsonMapper.toJsonString(req);
        System.out.println("NativeNet: req: " + json);

        byte[][] byteBuffers = call_utf8(stringToUtf8ByteArray(json), buffer);
        System.out.println("NativeNet: byteBuffers:" + Arrays.toString(byteBuffers));

        // read meta
        byte[] b0 = byteBuffers[0];
        if (b0 == null) {
            return Resp.of(500, "native net error");
        }
        String metaString = utf8ByteBufferToString(b0);
        System.out.println("NativeNet: metaString:" + metaString);
        Resp resp = jsonMapper.fromJsonString(metaString, Resp.class);

        // read body
        byte[] b1 = byteBuffers[1];
        resp.setByteBuffer(b1);
        return resp;
    }

    static native byte[][] call_utf8(byte[] jsonUtf8, Buffer buffer);

    static byte[] stringToUtf8ByteArray(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }

    static String utf8ByteBufferToString(byte[] buffer) {
        if (buffer == null) {
            return null;
        }
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public static class Req {
        private String url;
        private String method;
        private Map<String, String> headers;
        private Boolean followRedirects;

        public Req(String url, String method, Map<String, String> headers) {
            this.url = url;
            this.method = method;
            this.headers = headers;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public Boolean getFollowRedirects() {
            return followRedirects;
        }

        public void setFollowRedirects(Boolean followRedirects) {
            this.followRedirects = followRedirects;
        }
    }

    public static class Resp {
        private int code;
        private String message;
        private String error;
        private Map<String, String> headers;
        private byte[] byteBuffer;

        private String currentUrl;

        public static Resp of(int code, String error) {
            Resp resp = new Resp();
            resp.code = code;
            resp.error = error;
            return resp;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public byte[] getByteBuffer() {
            return byteBuffer;
        }

        public void setByteBuffer(byte[] byteBuffer) {
            this.byteBuffer = byteBuffer;
        }

        public String getCurrentUrl() {
            return currentUrl;
        }

        public void setCurrentUrl(String currentUrl) {
            this.currentUrl = currentUrl;
        }
    }
}
