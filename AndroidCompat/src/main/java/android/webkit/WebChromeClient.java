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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.view.View;

public class WebChromeClient {
    public WebChromeClient() {

    }

    public void onProgressChanged(WebView view, int newProgress) {
    }

    public void onReceivedTitle(WebView view, String title) {
    }

    public void onReceivedIcon(WebView view, Bitmap icon) {
    }

    public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
    }

    public void onShowCustomView(View view, CustomViewCallback callback) {
    }

    /** @deprecated */
    @Deprecated
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
    }

    public void onHideCustomView() {
    }

    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        return false;
    }

    public void onRequestFocus(WebView view) {
    }

    public void onCloseWindow(WebView window) {
    }

    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return false;
    }

    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return false;
    }

    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return false;
    }

    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {
    }

    /** @deprecated */
    @Deprecated
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
    }

    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
    }

    public void onGeolocationPermissionsHidePrompt() {
    }

    public void onPermissionRequest(PermissionRequest request) {
    }

    public void onPermissionRequestCanceled(PermissionRequest request) {
    }

    /** @deprecated */
    @Deprecated
    public boolean onJsTimeout() {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
    }

    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        return false;
    }

    @Nullable
    public Bitmap getDefaultVideoPoster() {
        return null;
    }

    @Nullable
    public View getVideoLoadingProgressView() {
        return null;
    }

    public void getVisitedHistory(ValueCallback<String[]> callback) {
    }

    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        return false;
    }

    public abstract static class FileChooserParams {
        public static final int MODE_OPEN = 0;
        public static final int MODE_OPEN_MULTIPLE = 1;
        public static final int MODE_SAVE = 3;

        public FileChooserParams() {
        }

        @Nullable
        public static Uri[] parseResult(int resultCode, Intent data) {
            return null;
        }

        public abstract int getMode();

        public abstract String[] getAcceptTypes();

        public abstract boolean isCaptureEnabled();

        @Nullable
        public abstract CharSequence getTitle();

        @Nullable
        public abstract String getFilenameHint();

        public abstract Intent createIntent();
    }

    public interface CustomViewCallback {
        void onCustomViewHidden();
    }
}
