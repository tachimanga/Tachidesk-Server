//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.webkit;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.annotation.Nullable;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.view.KeyEvent;

public class WebViewClient {
    public static final int ERROR_AUTHENTICATION = -4;
    public static final int ERROR_BAD_URL = -12;
    public static final int ERROR_CONNECT = -6;
    public static final int ERROR_FAILED_SSL_HANDSHAKE = -11;
    public static final int ERROR_FILE = -13;
    public static final int ERROR_FILE_NOT_FOUND = -14;
    public static final int ERROR_HOST_LOOKUP = -2;
    public static final int ERROR_IO = -7;
    public static final int ERROR_PROXY_AUTHENTICATION = -5;
    public static final int ERROR_REDIRECT_LOOP = -9;
    public static final int ERROR_TIMEOUT = -8;
    public static final int ERROR_TOO_MANY_REQUESTS = -15;
    public static final int ERROR_UNKNOWN = -1;
    public static final int ERROR_UNSAFE_RESOURCE = -16;
    public static final int ERROR_UNSUPPORTED_AUTH_SCHEME = -3;
    public static final int ERROR_UNSUPPORTED_SCHEME = -10;
    public static final int SAFE_BROWSING_THREAT_BILLING = 4;
    public static final int SAFE_BROWSING_THREAT_MALWARE = 1;
    public static final int SAFE_BROWSING_THREAT_PHISHING = 2;
    public static final int SAFE_BROWSING_THREAT_UNKNOWN = 0;
    public static final int SAFE_BROWSING_THREAT_UNWANTED_SOFTWARE = 3;

    public WebViewClient() {
    }

    /** @deprecated */
    @Deprecated
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
    }

    public void onPageFinished(WebView view, String url) {
    }

    public void onLoadResource(WebView view, String url) {
    }

    public void onPageCommitVisible(WebView view, String url) {
    }

    /** @deprecated */
    @Deprecated
    @Nullable
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return null;
    }

    @Nullable
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return null;
    }

    /** @deprecated */
    @Deprecated
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
        System.out.println("[STUB] WebViewClient.onTooManyRedirects");
    }

    /** @deprecated */
    @Deprecated
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        System.out.println("[STUB] WebViewClient.onReceivedError");
    }

    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        System.out.println("[STUB] WebViewClient.onReceivedError");
    }

    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        System.out.println("[STUB] WebViewClient.onReceivedHttpError");
    }

    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        System.out.println("[STUB] WebViewClient.onFormResubmission");
    }

    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        System.out.println("[STUB] WebViewClient.doUpdateVisitedHistory");
    }

    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        System.out.println("[STUB] WebViewClient.onReceivedSslError");
    }

    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        System.out.println("[STUB] WebViewClient.onReceivedClientCertRequest");
    }

    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        System.out.println("[STUB] WebViewClient.onReceivedHttpAuthRequest");
    }

    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        System.out.println("[STUB] WebViewClient.shouldOverrideKeyEvent");
        return false;
    }

    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        System.out.println("[STUB] WebViewClient.onUnhandledKeyEvent");
    }

    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        System.out.println("[STUB] WebViewClient.onScaleChanged");
    }

    public void onReceivedLoginRequest(WebView view, String realm, @Nullable String account, String args) {
        System.out.println("[STUB] WebViewClient.onReceivedLoginRequest");
    }

    public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
        System.out.println("[STUB] WebViewClient.onRenderProcessGone");
        return false;
    }

    public void onSafeBrowsingHit(WebView view, WebResourceRequest request, int threatType, SafeBrowsingResponse callback) {
        System.out.println("[STUB] WebViewClient.onSafeBrowsingHit");
    }
}
