package org.tachiyomi;

import io.javalin.plugin.json.JsonMapper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
 
public class NativeNet {
    public static Resp call(Req req, JsonMapper jsonMapper) {
        String json = jsonMapper.toJsonString(req);
        System.out.println("NativeNet:" + json);

        ByteBuffer[] byteBuffers = call_utf8(stringToUtf8ByteArray(json));
        System.out.println("NativeNet: byteBuffers:" + byteBuffers);
        if (byteBuffers.length == 0) {
            Resp resp = new Resp();
            resp.setCode(500);
            resp.setError("native net error");
            return resp;
        }

        // read meta
        ByteBuffer b0 = byteBuffers[0];
        String metaString = utf8ByteBufferToString(b0);
        System.out.println("NativeNet: metaString:" + metaString);
        Resp resp = jsonMapper.fromJsonString(metaString, Resp.class);

        // read body
        if (byteBuffers.length > 1) {
            ByteBuffer b1 = byteBuffers[1];
            byte[] buff = new byte[b1.remaining()];
            b1.get(buff);
            System.out.println("NativeNet: buff:" + buff.length);
            resp.setBody(buff);
        }
        return resp;
    }

    static native ByteBuffer[] call_utf8(byte[] jsonUtf8);

    static byte[] stringToUtf8ByteArray(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }

    static String utf8ByteBufferToString(ByteBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        byte[] buff = new byte[buffer.remaining()];
        buffer.get(buff);
        return new String(buff, StandardCharsets.UTF_8);
    }

    public static class Req {
        private String url;
        private String method;
        private Map<String, String> headers;

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
    }

    public static class Resp {
        private int code;
        private String message;
        private String error;
        private Map<String, String> headers;
        private long contentLength;
        private byte[] body;

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

        public long getContentLength() {
            return contentLength;
        }

        public void setContentLength(long contentLength) {
            this.contentLength = contentLength;
        }

        public byte[] getBody() {
            return body;
        }

        public void setBody(byte[] body) {
            this.body = body;
        }
    }
}
