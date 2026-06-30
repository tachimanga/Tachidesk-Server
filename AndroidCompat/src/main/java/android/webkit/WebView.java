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

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.NativeRef;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.print.PrintDocumentAdapter;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStructure;
import android.view.ViewTreeObserver;
import android.view.ViewDebug.ExportedProperty;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.autofill.AutofillValue;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.textclassifier.TextClassifier;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

public class WebView extends android.widget.AbsoluteLayout {
    public static final Map<Long, WebView> OBJ_MAP = new ConcurrentHashMap<>();

    public static final Map<String, Object> CALLBACK_MAP = new ConcurrentHashMap<>();

    public static final AtomicLong CALLBACK_ID = new AtomicLong(0);

    private final Map<String, Object> jsinterfaceMap = new ConcurrentHashMap<>();

    public static void onNativeCall(String[] inputs) {
        System.out.println("[NativeWeb]onNativeCall id=" + inputs[0] + ", method=" + inputs[1] + ", param=" + inputs[2]);
        long id = Long.parseLong(inputs[0]);
        String method = inputs[1];
        String param = inputs[2];

        WebView webView = OBJ_MAP.get(id);
        if (webView == null) {
            System.out.println("[NativeWeb]onNativeCall webview is null");
            return;
        }
        if (method == null) {
            System.out.println("[NativeWeb]onNativeCall method is null");
            return;
        }
        JSONObject jsonParam = new JSONObject();
        if (param != null && param.length() > 0) {
            jsonParam = new JSONObject(param);
        }
        switch (method) {
            case "onNativeLoadProgress":
                webView.onNativeLoadProgress(jsonParam);
                break;
            case "onEvaluateJavascriptResult":
                webView.onEvaluateJavascriptResult(jsonParam);
                break;
            case "didReceiveScriptMessage":
                webView.didReceiveScriptMessage(jsonParam);
                break;
            case "startURLSchemeTask":
               webView.onStartURLSchemeTask(jsonParam);
                break;
            default:
                System.out.println("[NativeWeb]onNativeCall unhandled method=" + method);
                break;
        }
    }

    private void onNativeLoadProgress(JSONObject param) {
        int progress = param.getInt("progress");
        System.out.println("[NativeWeb] onNativeLoadProgress progress="+progress);
        if (webViewClient != null && progress == 100) {
            // Notify the host application that the WebView will load the resource specified by the given url.
            webViewClient.onLoadResource(this, "");
            // Notify the host application that a page has finished loading.
            webViewClient.onPageFinished(this, "");
        }
        if (webViewClient != null && progress == 0) {
            // Notify the host application that a page has started loading.
            webViewClient.onPageStarted(this, "", new Bitmap());
        }
        if (webChromeClient != null) {
            webChromeClient.onProgressChanged(this, progress);
        }
    }

    private void onEvaluateJavascriptResult(JSONObject param) {
        String callbackId = param.getString("callbackId");
        Object object = CALLBACK_MAP.get(callbackId);
        if (object != null) {
            CALLBACK_MAP.remove(callbackId);
            ValueCallback<String> callback = (ValueCallback<String>)object;
            callback.onReceiveValue(param.isNull("result") ? null : param.getString("result"));
        }
    }

    private void didReceiveScriptMessage(JSONObject param) {
        String name = param.getString("name");
        Object jsInterface = this.jsinterfaceMap.get(name);
        if (jsInterface == null) {
            System.out.println("[NativeWeb] didReceiveScriptMessage jsInterface is null");
            return;
        }
        //jsinterfaceMap.remove(name);
        JSONObject body = param.getJSONObject("body");
        String method = body.getString("method");
        Method javaMethod = null;
        for (Method m : jsInterface.getClass().getDeclaredMethods()){
            if (m.getName().equals(method)) {
                m.setAccessible(true);
                javaMethod = m;
                break;
            }
        }
        if (javaMethod == null) {
            System.out.println("[NativeWeb] didReceiveScriptMessage javaMethod is null");
            return;
        }
        //System.out.println("[NativeWeb] didReceiveScriptMessage invoke...");
        JSONArray jsonArray = body.getJSONArray("args");
        Object[] args = new Object[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            args[i] = jsonArray.get(i);
        }
        try {
            javaMethod.invoke(jsInterface, args);
            System.out.println("[NativeWeb] didReceiveScriptMessage invoke done");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[NativeWeb] didReceiveScriptMessage invoke fail");
        }
    }

    private void onStartURLSchemeTask(JSONObject param) {
        if (webViewClient == null) {
            return;
        }
        System.out.println("[NativeWeb] onStartURLSchemeTask param=" + param);

        String url = param.getString("url");
        String method = param.getString("method");
        Map<String, String> headers = jsonObjectToMap(param.getJSONObject("headers"));

        // onLoadResource
        webViewClient.onLoadResource(this, url);

        // shouldInterceptRequest
        WebResourceResponse response = webViewClient.shouldInterceptRequest(this, new MyWebResourceRequest(url, method, headers));
        if (response == null) {
            webViewClient.shouldInterceptRequest(this, url);
        }
    }

    public static Map<String, String> jsonObjectToMap(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);
            if (value instanceof String) {
                map.put(key, (String) value);
            }
        }
        return map;
    }

    public static final int RENDERER_PRIORITY_BOUND = 1;
    public static final int RENDERER_PRIORITY_IMPORTANT = 2;
    public static final int RENDERER_PRIORITY_WAIVED = 0;
    public static final String SCHEME_GEO = "geo:0,0?q=";
    public static final String SCHEME_MAILTO = "mailto:";
    public static final String SCHEME_TEL = "tel:";

    private WebViewClient webViewClient;
    private WebChromeClient webChromeClient;
    private NativeRef nativeRef;

    private boolean needIntercept = false;

    public WebView(@NonNull Context context) {
        super(context);
        this.createNative();
    }

    public WebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context);
        this.createNative();
    }

    public WebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context);
        this.createNative();
    }

    public WebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context);
        this.createNative();
    }

    /** @deprecated */
    @Deprecated
    public WebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context);
        this.createNative();
    }

    private void createNative() {
        this.nativeRef = new NativeRef(this.createNativeWebview());
        OBJ_MAP.put(this.nativeRef.address(), this);
    }

    private String mCurrentUrl;
    private String mCurrentTitle;
    private int mProgress = 0;
    private int mContentHeight = 0;
    private int mInitialScale = 100;
    private float mScale = 1.0f;
    private WebBackForwardList mBackForwardList;
    private DownloadListener mDownloadListener;
    private WebViewRenderProcessClient mWebViewRenderProcessClient;
    private Executor mRenderProcessClientExecutor;
    private TextClassifier mTextClassifier;
    private FindListener mFindListener;
    private PictureListener mPictureListener;
    private Handler mHandler;

    /** @deprecated */
    @Deprecated
    public void setHorizontalScrollbarOverlay(boolean overlay) {
        System.out.println("[STUB] WebView.setHorizontalScrollbarOverlay");
    }

    /** @deprecated */
    @Deprecated
    public void setVerticalScrollbarOverlay(boolean overlay) {
        System.out.println("[STUB] WebView.setVerticalScrollbarOverlay");
    }

    /** @deprecated */
    @Deprecated
    public boolean overlayHorizontalScrollbar() {
        System.out.println("[STUB] WebView.overlayHorizontalScrollbar");
        return false;
    }

    /** @deprecated */
    @Deprecated
    public boolean overlayVerticalScrollbar() {
        System.out.println("[STUB] WebView.overlayVerticalScrollbar");
        return false;
    }

    @Nullable
    public SslCertificate getCertificate() {
        System.out.println("[STUB] WebView.getCertificate");
        return null;
    }

    /** @deprecated */
    @Deprecated
    public void setCertificate(SslCertificate certificate) {
        System.out.println("[STUB] WebView.setCertificate");
    }

    /** @deprecated */
    @Deprecated
    public void savePassword(String host, String username, String password) {
        System.out.println("[STUB] WebView.savePassword");
    }

    /** @deprecated */
    @Deprecated
    public void setHttpAuthUsernamePassword(String host, String realm, String username, String password) {
        System.out.println("[STUB] WebView.setHttpAuthUsernamePassword");
    }

    /** @deprecated */
    @Deprecated
    @Nullable
    public String[] getHttpAuthUsernamePassword(String host, String realm) {
        System.out.println("[STUB] WebView.getHttpAuthUsernamePassword");
        return null;
    }

    public void destroy() {
        System.out.println("[NativeWeb]destroy");
        if (this.nativeRef != null) {
            long addr = nativeRef.address();
            nativeRef.clear();
            if (addr != 0) {
                this.releaseNativeWebView(addr);
            }
            OBJ_MAP.remove(addr);
            this.nativeRef = null;
        }
    }

    protected void finalize() {
        System.out.println("[NativeWeb]finalize");
        destroy();
    }

    public void setNetworkAvailable(boolean networkUp) {
        System.out.println("[STUB] WebView.setNetworkAvailable");
    }

    @Nullable
    public WebBackForwardList saveState(@NonNull Bundle outState) {
        System.out.println("[STUB] WebView.saveState");
        return mBackForwardList;
    }

    @Nullable
    public WebBackForwardList restoreState(@NonNull Bundle inState) {
        System.out.println("[STUB] WebView.restoreState");
        return mBackForwardList;
    }

    public void loadUrl(@NonNull String url, @NonNull Map<String, String> additionalHttpHeaders) {
        System.out.println("[NativeWeb]loadUrl url: " + url + ", headers: " + additionalHttpHeaders);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", url);
        if (needIntercept) {
            jsonObject.put("needIntercept", "1");
        }
        if (additionalHttpHeaders != null) {
            jsonObject.put("headers", new JSONObject(additionalHttpHeaders));
        }

        this.invokeNativeMethod("loadUrl", jsonObject);
    }

    public void loadUrl(@NonNull String url) {
        this.loadUrl(url, null);
    }

    public void postUrl(@NonNull String url, @NonNull byte[] postData) {
        System.out.println("[STUB] WebView.postUrl");
    }

    public void loadData(@NonNull String data, @Nullable String mimeType, @Nullable String encoding) {
        System.out.println("[STUB] WebView.loadData");
    }

    public void loadDataWithBaseURL(@Nullable String baseUrl, @NonNull String data, @Nullable String mimeType, @Nullable String encoding, @Nullable String historyUrl) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("baseUrl", baseUrl);
        jsonObject.put("data", data);
        jsonObject.put("mimeType", mimeType);
        jsonObject.put("encoding", encoding);
        jsonObject.put("historyUrl", historyUrl);
        if (needIntercept) {
            jsonObject.put("needIntercept", "1");
        }
        this.invokeNativeMethod("loadDataWithBaseURL", jsonObject);
    }

    public void evaluateJavascript(@NonNull String script, @Nullable ValueCallback<String> resultCallback) {
        long id = CALLBACK_ID.incrementAndGet();
        if (resultCallback != null) {
            CALLBACK_MAP.put(id + "", resultCallback);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("script", script);
        jsonObject.put("callbackId", id);
        this.invokeNativeMethod("evaluateJavascript", jsonObject);
    }

    public void saveWebArchive(@NonNull String filename) {
        System.out.println("[STUB] WebView.saveWebArchive");
    }

    public void saveWebArchive(@NonNull String basename, boolean autoname, @Nullable ValueCallback<String> callback) {
        System.out.println("[STUB] WebView.saveWebArchive");
    }

    public void stopLoading() {

    }

    public void reload() {
        System.out.println("[STUB] WebView.reload");
    }

    public boolean canGoBack() {
        System.out.println("[STUB] WebView.canGoBack");
        return false;
    }

    public void goBack() {
        System.out.println("[STUB] WebView.goBack");
    }

    public boolean canGoForward() {
        System.out.println("[STUB] WebView.canGoForward");
        return false;
    }

    public void goForward() {
        System.out.println("[STUB] WebView.goForward");
    }

    public boolean canGoBackOrForward(int steps) {
        System.out.println("[STUB] WebView.canGoBackOrForward");
        return false;
    }

    public void goBackOrForward(int steps) {
        System.out.println("[STUB] WebView.goBackOrForward");
    }

    public boolean isPrivateBrowsingEnabled() {
        System.out.println("[STUB] WebView.isPrivateBrowsingEnabled");
        return false;
    }

    public boolean pageUp(boolean top) {
        System.out.println("[STUB] WebView.pageUp");
        return false;
    }

    public boolean pageDown(boolean bottom) {
        System.out.println("[STUB] WebView.pageDown");
        return false;
    }

    public void postVisualStateCallback(long requestId, @NonNull VisualStateCallback callback) {
        System.out.println("[STUB] WebView.postVisualStateCallback");
    }

    /** @deprecated */
    @Deprecated
    public void clearView() {
        System.out.println("[STUB] WebView.clearView");
    }

    /** @deprecated */
    @Deprecated
    public Picture capturePicture() {
        System.out.println("[STUB] WebView.capturePicture");
        return null;
    }

    /** @deprecated */
    @Deprecated
    public PrintDocumentAdapter createPrintDocumentAdapter() {
        System.out.println("[STUB] WebView.createPrintDocumentAdapter");
        return null;
    }

    @NonNull
    public PrintDocumentAdapter createPrintDocumentAdapter(@NonNull String documentName) {
        System.out.println("[STUB] WebView.createPrintDocumentAdapter");
        return null;
    }

    /** @deprecated */
    @Deprecated
    @ExportedProperty(
            category = "webview"
    )
    public float getScale() {
        return mScale;
    }

    public void setInitialScale(int scaleInPercent) {
        mInitialScale = scaleInPercent;
    }

    public void invokeZoomPicker() {
        System.out.println("[STUB] WebView.invokeZoomPicker");
    }

    @NonNull
    public HitTestResult getHitTestResult() {
        System.out.println("[STUB] WebView.getHitTestResult");
        return new HitTestResult();
    }

    public void requestFocusNodeHref(@Nullable Message hrefMsg) {
        System.out.println("[STUB] WebView.requestFocusNodeHref");
    }

    public void requestImageRef(@NonNull Message msg) {
        System.out.println("[STUB] WebView.requestImageRef");
    }

    @ExportedProperty(
            category = "webview"
    )
    @Nullable
    public String getUrl() {
        return mCurrentUrl;
    }

    @ExportedProperty(
            category = "webview"
    )
    @Nullable
    public String getOriginalUrl() {
        return mCurrentUrl;
    }

    @ExportedProperty(
            category = "webview"
    )
    @Nullable
    public String getTitle() {
        return mCurrentTitle;
    }

    @Nullable
    public Bitmap getFavicon() {
        System.out.println("[STUB] WebView.getFavicon");
        return null;
    }

    public int getProgress() {
        return mProgress;
    }

    @ExportedProperty(
            category = "webview"
    )
    public int getContentHeight() {
        return mContentHeight;
    }

    public void pauseTimers() {
        System.out.println("[STUB] WebView.pauseTimers");
    }

    public void resumeTimers() {
        System.out.println("[STUB] WebView.resumeTimers");
    }

    public void onPause() {
        System.out.println("[STUB] WebView.onPause");
    }

    public void onResume() {
        System.out.println("[STUB] WebView.onResume");
    }

    /** @deprecated */
    @Deprecated
    public void freeMemory() {
        System.out.println("[STUB] WebView.freeMemory");
    }

    public void clearCache(boolean includeDiskFiles) {
        System.out.println("[STUB] WebView.clearCache");
    }

    public void clearFormData() {
        System.out.println("[STUB] WebView.clearFormData");
    }

    public void clearHistory() {
        System.out.println("[STUB] WebView.clearHistory");
    }

    public void clearSslPreferences() {
        System.out.println("[STUB] WebView.clearSslPreferences");
    }

    public static void clearClientCertPreferences(@Nullable Runnable onCleared) {
        System.out.println("[STUB] WebView.clearClientCertPreferences");
    }

    public static void startSafeBrowsing(@NonNull Context context, @Nullable ValueCallback<Boolean> callback) {
        System.out.println("[STUB] WebView.startSafeBrowsing");
    }

    public static void setSafeBrowsingWhitelist(@NonNull List<String> hosts, @Nullable ValueCallback<Boolean> callback) {
        System.out.println("[STUB] WebView.setSafeBrowsingWhitelist");
    }

    @NonNull
    public static Uri getSafeBrowsingPrivacyPolicyUrl() {
        System.out.println("[STUB] WebView.getSafeBrowsingPrivacyPolicyUrl");
        return null;
    }

    @NonNull
    public WebBackForwardList copyBackForwardList() {
        System.out.println("[STUB] WebView.copyBackForwardList");
        return null;
    }

    public void setFindListener(@Nullable FindListener listener) {
        mFindListener = listener;
    }

    public void findNext(boolean forward) {
        System.out.println("[STUB] WebView.findNext");
    }

    /** @deprecated */
    @Deprecated
    public int findAll(String find) {
        System.out.println("[STUB] WebView.findAll");
        return 0;
    }

    public void findAllAsync(@NonNull String find) {
        System.out.println("[STUB] WebView.findAllAsync");
    }

    /** @deprecated */
    @Deprecated
    public boolean showFindDialog(@Nullable String text, boolean showIme) {
        System.out.println("[STUB] WebView.showFindDialog");
        return false;
    }

    /** @deprecated */
    @Deprecated
    @Nullable
    public static String findAddress(String addr) {
        System.out.println("[STUB] WebView.findAddress");
        return null;
    }

    public static void enableSlowWholeDocumentDraw() {
        System.out.println("[STUB] WebView.enableSlowWholeDocumentDraw");
    }

    public void clearMatches() {
        System.out.println("[STUB] WebView.clearMatches");
    }

    public void documentHasImages(@NonNull Message response) {
        System.out.println("[STUB] WebView.documentHasImages");
    }

    public void setWebViewClient(@NonNull WebViewClient client) {
        boolean hasCustomShouldInterceptRequest = hasCustomShouldInterceptRequest(client);
        boolean hasCustomOnLoadResource = hasCustomOnLoadResource(client);
        this.needIntercept = hasCustomShouldInterceptRequest || hasCustomOnLoadResource;
        System.out.println("[NativeWeb] needIntercept=" + needIntercept
                + ", hasCustomShouldInterceptRequest" + hasCustomShouldInterceptRequest
                + ", hasCustomOnLoadResource" + hasCustomOnLoadResource
        );
        this.webViewClient = client;
    }

    private boolean hasCustomShouldInterceptRequest(WebViewClient client) {
        boolean found = false;
        Class<?> currentClass = client.getClass();
        while (currentClass != null) {
            try {
                Method method = currentClass.getDeclaredMethod("shouldInterceptRequest", WebView.class, WebResourceRequest.class);
                found = true;
            } catch (NoSuchMethodException e) {
            }
            try {
                Method method = currentClass.getDeclaredMethod("shouldInterceptRequest", WebView.class, String.class);
                found = true;
            } catch (NoSuchMethodException e) {
            }
            currentClass = currentClass.getSuperclass();
            if (currentClass == WebViewClient.class ||
                    currentClass == Object.class) {
                break;
            }
        }
        return found;
    }

    private boolean hasCustomOnLoadResource(WebViewClient client) {
        boolean found = false;
        Class<?> currentClass = client.getClass();
        while (currentClass != null) {
            try {
                Method method = currentClass.getDeclaredMethod("onLoadResource", WebView.class, String.class);
                found = true;
            } catch (NoSuchMethodException e) {
            }
            currentClass = currentClass.getSuperclass();
            if (currentClass == WebViewClient.class ||
                    currentClass == Object.class) {
                break;
            }
        }
        return found;
    }

    @NonNull
    public WebViewClient getWebViewClient() {
        return this.webViewClient;
    }

    @Nullable
    public WebViewRenderProcess getWebViewRenderProcess() {
        System.out.println("[STUB] WebView.getWebViewRenderProcess");
        return null;
    }

    public void setWebViewRenderProcessClient(@NonNull Executor executor, @NonNull WebViewRenderProcessClient webViewRenderProcessClient) {
        mRenderProcessClientExecutor = executor;
        mWebViewRenderProcessClient = webViewRenderProcessClient;
    }

    public void setWebViewRenderProcessClient(@Nullable WebViewRenderProcessClient webViewRenderProcessClient) {
        mWebViewRenderProcessClient = webViewRenderProcessClient;
    }

    @Nullable
    public WebViewRenderProcessClient getWebViewRenderProcessClient() {
        return mWebViewRenderProcessClient;
    }

    public void setDownloadListener(@Nullable DownloadListener listener) {
        mDownloadListener = listener;
    }

    public void setWebChromeClient(@Nullable WebChromeClient client) {
        this.webChromeClient = client;
    }

    @Nullable
    public WebChromeClient getWebChromeClient() {
        return this.webChromeClient;
    }

    /** @deprecated */
    @Deprecated
    public void setPictureListener(PictureListener listener) {
        mPictureListener = listener;
    }

    public void addJavascriptInterface(@NonNull Object object, @NonNull String name) {
        jsinterfaceMap.put(name, object);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        this.invokeNativeMethod("addJavascriptInterface", jsonObject);
    }

    public void removeJavascriptInterface(@NonNull String name) {
        jsinterfaceMap.remove(name);
    }

    @NonNull
    public WebMessagePort[] createWebMessageChannel() {
        System.out.println("[STUB] WebView.createWebMessageChannel");
        return new WebMessagePort[0];
    }

    public void postWebMessage(@NonNull WebMessage message, @NonNull Uri targetOrigin) {
        System.out.println("[STUB] WebView.postWebMessage");
    }

    @NonNull
    public WebSettings getSettings() {
        return new MyWebSettings();
    }

    public static void setWebContentsDebuggingEnabled(boolean enabled) {
        System.out.println("[STUB] WebView.setWebContentsDebuggingEnabled");
    }

    public static void setDataDirectorySuffix(@NonNull String suffix) {
        System.out.println("[STUB] WebView.setDataDirectorySuffix");
    }

    public static void disableWebView() {
        System.out.println("[STUB] WebView.disableWebView");
    }

    /** @deprecated */
    @Deprecated
    public void onChildViewAdded(View parent, View child) {
        System.out.println("[STUB] WebView.onChildViewAdded");
    }

    /** @deprecated */
    @Deprecated
    public void onChildViewRemoved(View p, View child) {
        System.out.println("[STUB] WebView.onChildViewRemoved");
    }

    /** @deprecated */
    @Deprecated
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        System.out.println("[STUB] WebView.onGlobalFocusChanged");
    }

    /** @deprecated */
    @Deprecated
    public void setMapTrackballToArrowKeys(boolean setMap) {
        System.out.println("[STUB] WebView.setMapTrackballToArrowKeys");
    }

    public void flingScroll(int vx, int vy) {
        System.out.println("[STUB] WebView.flingScroll");
    }

    /** @deprecated */
    @Deprecated
    public boolean canZoomIn() {
        System.out.println("[STUB] WebView.canZoomIn");
        return false;
    }

    /** @deprecated */
    @Deprecated
    public boolean canZoomOut() {
        System.out.println("[STUB] WebView.canZoomOut");
        return false;
    }

    public void zoomBy(float zoomFactor) {
        System.out.println("[STUB] WebView.zoomBy");
    }

    public boolean zoomIn() {
        System.out.println("[STUB] WebView.zoomIn");
        return false;
    }

    public boolean zoomOut() {
        System.out.println("[STUB] WebView.zoomOut");
        return false;
    }

    public void setRendererPriorityPolicy(int rendererRequestedPriority, boolean waivedWhenNotVisible) {
        System.out.println("[STUB] WebView.setRendererPriorityPolicy");
    }

    public int getRendererRequestedPriority() {
        System.out.println("[STUB] WebView.getRendererRequestedPriority");
        return RENDERER_PRIORITY_IMPORTANT;
    }

    public boolean getRendererPriorityWaivedWhenNotVisible() {
        System.out.println("[STUB] WebView.getRendererPriorityWaivedWhenNotVisible");
        return false;
    }

    public void setTextClassifier(@Nullable TextClassifier textClassifier) {
        mTextClassifier = textClassifier;
    }

    @NonNull
    public TextClassifier getTextClassifier() {
        if (mTextClassifier != null) {
            return mTextClassifier;
        }
        System.out.println("[STUB] WebView.getTextClassifier");
        return null;
    }

    @NonNull
    public static ClassLoader getWebViewClassLoader() {
        System.out.println("[STUB] WebView.getWebViewClassLoader");
        return WebView.class.getClassLoader();
    }

    @NonNull
    public Looper getWebViewLooper() {
        System.out.println("[STUB] WebView.getWebViewLooper");
        return Looper.getMainLooper();
    }

    protected void onAttachedToWindow() {
        System.out.println("[STUB] WebView.onAttachedToWindow");
    }

    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
    }

    public void setOverScrollMode(int mode) {
        super.setOverScrollMode(mode);
    }

    public void setScrollBarStyle(int style) {
        super.setScrollBarStyle(style);
    }

    protected int computeHorizontalScrollRange() {
        System.out.println("[STUB] WebView.computeHorizontalScrollRange");
        return 0;
    }

    protected int computeHorizontalScrollOffset() {
        System.out.println("[STUB] WebView.computeHorizontalScrollOffset");
        return 0;
    }

    protected int computeVerticalScrollRange() {
        System.out.println("[STUB] WebView.computeVerticalScrollRange");
        return 0;
    }

    protected int computeVerticalScrollOffset() {
        System.out.println("[STUB] WebView.computeVerticalScrollOffset");
        return 0;
    }

    protected int computeVerticalScrollExtent() {
        System.out.println("[STUB] WebView.computeVerticalScrollExtent");
        return 0;
    }

    public void computeScroll() {
        System.out.println("[STUB] WebView.computeScroll");
    }

    public boolean onHoverEvent(MotionEvent event) {
        System.out.println("[STUB] WebView.onHoverEvent");
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        System.out.println("[STUB] WebView.onTouchEvent");
        return false;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        System.out.println("[STUB] WebView.onGenericMotionEvent");
        return false;
    }

    public boolean onTrackballEvent(MotionEvent event) {
        System.out.println("[STUB] WebView.onTrackballEvent");
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        System.out.println("[STUB] WebView.onKeyDown");
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        System.out.println("[STUB] WebView.onKeyUp");
        return false;
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        System.out.println("[STUB] WebView.onKeyMultiple");
        return false;
    }

    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        System.out.println("[STUB] WebView.getAccessibilityNodeProvider");
        return null;
    }

    /** @deprecated */
    @Deprecated
    public boolean shouldDelayChildPressedState() {
        System.out.println("[STUB] WebView.shouldDelayChildPressedState");
        return true;
    }

    public CharSequence getAccessibilityClassName() {
        System.out.println("[STUB] WebView.getAccessibilityClassName");
        return "WebView";
    }

    public void onProvideVirtualStructure(ViewStructure structure) {
        System.out.println("[STUB] WebView.onProvideVirtualStructure");
    }

    public void onProvideAutofillVirtualStructure(ViewStructure structure, int flags) {
        System.out.println("[STUB] WebView.onProvideAutofillVirtualStructure");
    }

    public void onProvideContentCaptureStructure(@NonNull ViewStructure structure, int flags) {
        System.out.println("[STUB] WebView.onProvideContentCaptureStructure");
    }

    public void autofill(SparseArray<AutofillValue> values) {
        System.out.println("[STUB] WebView.autofill");
    }

    public boolean isVisibleToUserForAutofill(int virtualId) {
        System.out.println("[STUB] WebView.isVisibleToUserForAutofill");
        return false;
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        System.out.println("[STUB] WebView.onOverScrolled");
    }

    protected void onWindowVisibilityChanged(int visibility) {
        System.out.println("[STUB] WebView.onWindowVisibilityChanged");
    }

    protected void onDraw(Canvas canvas) {
        System.out.println("[STUB] WebView.onDraw");
    }

    public boolean performLongClick() {
        System.out.println("[STUB] WebView.performLongClick");
        return false;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        System.out.println("[STUB] WebView.onConfigurationChanged");
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        System.out.println("[STUB] WebView.onCreateInputConnection");
        return null;
    }

    public boolean onDragEvent(DragEvent event) {
        System.out.println("[STUB] WebView.onDragEvent");
        return false;
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        System.out.println("[STUB] WebView.onVisibilityChanged");
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        System.out.println("[STUB] WebView.onWindowFocusChanged");
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        System.out.println("[STUB] WebView.onFocusChanged");
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        System.out.println("[STUB] WebView.onSizeChanged");
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        System.out.println("[STUB] WebView.onScrollChanged");
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        System.out.println("[STUB] WebView.dispatchKeyEvent");
        return false;
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        System.out.println("[STUB] WebView.requestFocus");
        return false;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        System.out.println("[STUB] WebView.onMeasure");
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        System.out.println("[STUB] WebView.requestChildRectangleOnScreen");
        return false;
    }

    public void setBackgroundColor(int color) {
        System.out.println("[STUB] WebView.setBackgroundColor");
        super.setBackgroundColor(color);
    }

    public void setLayerType(int layerType, Paint paint) {
    }

    protected void dispatchDraw(Canvas canvas) {
        System.out.println("[STUB] WebView.dispatchDraw");
    }

    public void onStartTemporaryDetach() {
        System.out.println("[STUB] WebView.onStartTemporaryDetach");
    }

    public void onFinishTemporaryDetach() {
        System.out.println("[STUB] WebView.onFinishTemporaryDetach");
    }

    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        return mHandler;
    }

    public View findFocus() {
        System.out.println("[STUB] WebView.findFocus");
        return super.findFocus();
    }

    @Nullable
    public static PackageInfo getCurrentWebViewPackage() {
        System.out.println("[STUB] WebView.getCurrentWebViewPackage");
        return null;
    }

    public boolean onCheckIsTextEditor() {
        System.out.println("[STUB] WebView.onCheckIsTextEditor");
        return false;
    }

    public class WebViewTransport {
        private WebView mWebView;

        public WebViewTransport() {
        }

        public synchronized void setWebView(@Nullable WebView webview) {
            mWebView = webview;
        }

        @Nullable
        public synchronized WebView getWebView() {
            return mWebView;
        }
    }

    public abstract static class VisualStateCallback {
        public VisualStateCallback() {
        }

        public abstract void onComplete(long var1);
    }

    /** @deprecated */
    @Deprecated
    public interface PictureListener {
        /** @deprecated */
        @Deprecated
        void onNewPicture(WebView var1, @Nullable Picture var2);
    }

    public static class HitTestResult {
        /** @deprecated */
        @Deprecated
        public static final int ANCHOR_TYPE = 1;
        public static final int EDIT_TEXT_TYPE = 9;
        public static final int EMAIL_TYPE = 4;
        public static final int GEO_TYPE = 3;
        /** @deprecated */
        @Deprecated
        public static final int IMAGE_ANCHOR_TYPE = 6;
        public static final int IMAGE_TYPE = 5;
        public static final int PHONE_TYPE = 2;
        public static final int SRC_ANCHOR_TYPE = 7;
        public static final int SRC_IMAGE_ANCHOR_TYPE = 8;
        public static final int UNKNOWN_TYPE = 0;

        private int mType = UNKNOWN_TYPE;
        private String mExtra;

        HitTestResult() {
        }

        HitTestResult(int type, String extra) {
            mType = type;
            mExtra = extra;
        }

        public int getType() {
            return mType;
        }

        @Nullable
        public String getExtra() {
            return mExtra;
        }
    }

    public interface FindListener {
        void onFindResultReceived(int var1, int var2, boolean var3);
    }

    private native long createNativeWebview();

    private void invokeNativeMethod(String method, JSONObject param) {
        if (this.nativeRef == null) {
            System.out.println("[NativeWeb] nativeRef is null");
            return;
        }
        //System.out.println("[NativeWeb] invokeNative method:" + method + ", param=" + param);
        System.out.println("[NativeWeb] invokeNative method:" + method);
        long ref = this.nativeRef.address();

        byte[] methodBytes = method.getBytes(StandardCharsets.UTF_8);

        String jsonString = param.toString();
        byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);

        this.invokeNative(ref, methodBytes, bytes);
    }

    private native void invokeNative(long ref, byte[] method, byte[] reqJsonUtf8);

    private native void releaseNativeWebView(long ref);
}
