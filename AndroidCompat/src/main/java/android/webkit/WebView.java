//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.webkit;

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
import android.widget.AbsoluteLayout;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

public class WebView  {
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
        jsinterfaceMap.remove(name);
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

    public static final int RENDERER_PRIORITY_BOUND = 1;
    public static final int RENDERER_PRIORITY_IMPORTANT = 2;
    public static final int RENDERER_PRIORITY_WAIVED = 0;
    public static final String SCHEME_GEO = "geo:0,0?q=";
    public static final String SCHEME_MAILTO = "mailto:";
    public static final String SCHEME_TEL = "tel:";

    private WebViewClient webViewClient;
    private WebChromeClient webChromeClient;
    private NativeRef nativeRef;

    public WebView(@NonNull Context context) {
        this.createNative();
    }

    public WebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this.createNative();
    }

    public WebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this.createNative();
    }

    public WebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.createNative();
    }
    /** @deprecated */
    @Deprecated
    public WebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        this.createNative();
    }

    private void createNative() {
        this.nativeRef = new NativeRef(this.createNativeWebview());
        OBJ_MAP.put(this.nativeRef.address(), this);
    }

    /** @deprecated */
    @Deprecated
    public void setHorizontalScrollbarOverlay(boolean overlay) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void setVerticalScrollbarOverlay(boolean overlay) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public boolean overlayHorizontalScrollbar() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public boolean overlayVerticalScrollbar() {
        throw new RuntimeException("Stub!");
    }

    @Nullable
    public SslCertificate getCertificate() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void setCertificate(SslCertificate certificate) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void savePassword(String host, String username, String password) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void setHttpAuthUsernamePassword(String host, String realm, String username, String password) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    @Nullable
    public String[] getHttpAuthUsernamePassword(String host, String realm) {
        throw new RuntimeException("Stub!");
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
        throw new RuntimeException("Stub!");
    }

    @Nullable
    public WebBackForwardList saveState(@NonNull Bundle outState) {
        throw new RuntimeException("Stub!");
    }

    @Nullable
    public WebBackForwardList restoreState(@NonNull Bundle inState) {
        throw new RuntimeException("Stub!");
    }

    public void loadUrl(@NonNull String url, @NonNull Map<String, String> additionalHttpHeaders) {
        System.out.println("[NativeWeb]loadUrl url: " + url + ", headers: " + additionalHttpHeaders);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", url);
        if (additionalHttpHeaders != null) {
            jsonObject.put("headers", new JSONObject(additionalHttpHeaders));
        }

        this.invokeNativeMethod("loadUrl", jsonObject);
    }

    public void loadUrl(@NonNull String url) {
        this.loadUrl(url, null);
    }

    public void postUrl(@NonNull String url, @NonNull byte[] postData) {
        throw new RuntimeException("Stub!");
    }

    public void loadData(@NonNull String data, @Nullable String mimeType, @Nullable String encoding) {
        throw new RuntimeException("Stub!");
    }

    public void loadDataWithBaseURL(@Nullable String baseUrl, @NonNull String data, @Nullable String mimeType, @Nullable String encoding, @Nullable String historyUrl) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("baseUrl", baseUrl);
        jsonObject.put("data", data);
        jsonObject.put("mimeType", mimeType);
        jsonObject.put("encoding", encoding);
        jsonObject.put("historyUrl", historyUrl);
        this.invokeNativeMethod("loadDataWithBaseURL", jsonObject);
    }

    public void evaluateJavascript(@NonNull String script, @Nullable ValueCallback<String> resultCallback) {
        long id = CALLBACK_ID.incrementAndGet();
        CALLBACK_MAP.put(id + "", resultCallback);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("script", script);
        jsonObject.put("callbackId", id);
        this.invokeNativeMethod("evaluateJavascript", jsonObject);
    }

    public void saveWebArchive(@NonNull String filename) {
        throw new RuntimeException("Stub!");
    }

    public void saveWebArchive(@NonNull String basename, boolean autoname, @Nullable ValueCallback<String> callback) {
        throw new RuntimeException("Stub!");
    }

    public void stopLoading() {

    }

    public void reload() {
        throw new RuntimeException("Stub!");
    }

    public boolean canGoBack() {
        throw new RuntimeException("Stub!");
    }

    public void goBack() {
        throw new RuntimeException("Stub!");
    }

    public boolean canGoForward() {
        throw new RuntimeException("Stub!");
    }

    public void goForward() {
        throw new RuntimeException("Stub!");
    }

    public boolean canGoBackOrForward(int steps) {
        throw new RuntimeException("Stub!");
    }

    public void goBackOrForward(int steps) {
        throw new RuntimeException("Stub!");
    }

    public boolean isPrivateBrowsingEnabled() {
        throw new RuntimeException("Stub!");
    }

    public boolean pageUp(boolean top) {
        throw new RuntimeException("Stub!");
    }

    public boolean pageDown(boolean bottom) {
        throw new RuntimeException("Stub!");
    }

    public void postVisualStateCallback(long requestId, @NonNull VisualStateCallback callback) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void clearView() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public Picture capturePicture() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public PrintDocumentAdapter createPrintDocumentAdapter() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public PrintDocumentAdapter createPrintDocumentAdapter(@NonNull String documentName) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    @ExportedProperty(
            category = "webview"
    )
    public float getScale() {
        throw new RuntimeException("Stub!");
    }

    public void setInitialScale(int scaleInPercent) {
        throw new RuntimeException("Stub!");
    }

    public void invokeZoomPicker() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public HitTestResult getHitTestResult() {
        throw new RuntimeException("Stub!");
    }

    public void requestFocusNodeHref(@Nullable Message hrefMsg) {
        throw new RuntimeException("Stub!");
    }

    public void requestImageRef(@NonNull Message msg) {
        throw new RuntimeException("Stub!");
    }

    @ExportedProperty(
            category = "webview"
    )
    @Nullable
    public String getUrl() {
        throw new RuntimeException("Stub!");
    }

    @ExportedProperty(
            category = "webview"
    )
    @Nullable
    public String getOriginalUrl() {
        throw new RuntimeException("Stub!");
    }

    @ExportedProperty(
            category = "webview"
    )
    @Nullable
    public String getTitle() {
        throw new RuntimeException("Stub!");
    }

    @Nullable
    public Bitmap getFavicon() {
        throw new RuntimeException("Stub!");
    }

    public int getProgress() {
        throw new RuntimeException("Stub!");
    }

    @ExportedProperty(
            category = "webview"
    )
    public int getContentHeight() {
        throw new RuntimeException("Stub!");
    }

    public void pauseTimers() {
        throw new RuntimeException("Stub!");
    }

    public void resumeTimers() {
        throw new RuntimeException("Stub!");
    }

    public void onPause() {
        throw new RuntimeException("Stub!");
    }

    public void onResume() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void freeMemory() {
        throw new RuntimeException("Stub!");
    }

    public void clearCache(boolean includeDiskFiles) {
        throw new RuntimeException("Stub!");
    }

    public void clearFormData() {
        throw new RuntimeException("Stub!");
    }

    public void clearHistory() {
        throw new RuntimeException("Stub!");
    }

    public void clearSslPreferences() {
        throw new RuntimeException("Stub!");
    }

    public static void clearClientCertPreferences(@Nullable Runnable onCleared) {
        throw new RuntimeException("Stub!");
    }

    public static void startSafeBrowsing(@NonNull Context context, @Nullable ValueCallback<Boolean> callback) {
        throw new RuntimeException("Stub!");
    }

    public static void setSafeBrowsingWhitelist(@NonNull List<String> hosts, @Nullable ValueCallback<Boolean> callback) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public static Uri getSafeBrowsingPrivacyPolicyUrl() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public WebBackForwardList copyBackForwardList() {
        throw new RuntimeException("Stub!");
    }

    public void setFindListener(@Nullable FindListener listener) {
        throw new RuntimeException("Stub!");
    }

    public void findNext(boolean forward) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public int findAll(String find) {
        throw new RuntimeException("Stub!");
    }

    public void findAllAsync(@NonNull String find) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public boolean showFindDialog(@Nullable String text, boolean showIme) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    @Nullable
    public static String findAddress(String addr) {
        throw new RuntimeException("Stub!");
    }

    public static void enableSlowWholeDocumentDraw() {
        throw new RuntimeException("Stub!");
    }

    public void clearMatches() {
        throw new RuntimeException("Stub!");
    }

    public void documentHasImages(@NonNull Message response) {
        throw new RuntimeException("Stub!");
    }

    public void setWebViewClient(@NonNull WebViewClient client) {
        this.webViewClient = client;
    }

    @NonNull
    public WebViewClient getWebViewClient() {
        return this.webViewClient;
    }

    @Nullable
    public WebViewRenderProcess getWebViewRenderProcess() {
        throw new RuntimeException("Stub!");
    }

    public void setWebViewRenderProcessClient(@NonNull Executor executor, @NonNull WebViewRenderProcessClient webViewRenderProcessClient) {
        throw new RuntimeException("Stub!");
    }

    public void setWebViewRenderProcessClient(@Nullable WebViewRenderProcessClient webViewRenderProcessClient) {
        throw new RuntimeException("Stub!");
    }

    @Nullable
    public WebViewRenderProcessClient getWebViewRenderProcessClient() {
        throw new RuntimeException("Stub!");
    }

    public void setDownloadListener(@Nullable DownloadListener listener) {
        throw new RuntimeException("Stub!");
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
        throw new RuntimeException("Stub!");
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
        throw new RuntimeException("Stub!");
    }

    public void postWebMessage(@NonNull WebMessage message, @NonNull Uri targetOrigin) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public WebSettings getSettings() {
        return new MyWebSettings();
    }

    public static void setWebContentsDebuggingEnabled(boolean enabled) {
        throw new RuntimeException("Stub!");
    }

    public static void setDataDirectorySuffix(@NonNull String suffix) {
        throw new RuntimeException("Stub!");
    }

    public static void disableWebView() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void onChildViewAdded(View parent, View child) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void onChildViewRemoved(View p, View child) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void setMapTrackballToArrowKeys(boolean setMap) {
        throw new RuntimeException("Stub!");
    }

    public void flingScroll(int vx, int vy) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public boolean canZoomIn() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public boolean canZoomOut() {
        throw new RuntimeException("Stub!");
    }

    public void zoomBy(float zoomFactor) {
        throw new RuntimeException("Stub!");
    }

    public boolean zoomIn() {
        throw new RuntimeException("Stub!");
    }

    public boolean zoomOut() {
        throw new RuntimeException("Stub!");
    }

    public void setRendererPriorityPolicy(int rendererRequestedPriority, boolean waivedWhenNotVisible) {
        throw new RuntimeException("Stub!");
    }

    public int getRendererRequestedPriority() {
        throw new RuntimeException("Stub!");
    }

    public boolean getRendererPriorityWaivedWhenNotVisible() {
        throw new RuntimeException("Stub!");
    }

    public void setTextClassifier(@Nullable TextClassifier textClassifier) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public TextClassifier getTextClassifier() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public static ClassLoader getWebViewClassLoader() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public Looper getWebViewLooper() {
        throw new RuntimeException("Stub!");
    }

    protected void onAttachedToWindow() {
        throw new RuntimeException("Stub!");
    }

    public void setLayoutParams(ViewGroup.LayoutParams params) {
        throw new RuntimeException("Stub!");
    }

    public void setOverScrollMode(int mode) {
        throw new RuntimeException("Stub!");
    }

    public void setScrollBarStyle(int style) {
        throw new RuntimeException("Stub!");
    }

    protected int computeHorizontalScrollRange() {
        throw new RuntimeException("Stub!");
    }

    protected int computeHorizontalScrollOffset() {
        throw new RuntimeException("Stub!");
    }

    protected int computeVerticalScrollRange() {
        throw new RuntimeException("Stub!");
    }

    protected int computeVerticalScrollOffset() {
        throw new RuntimeException("Stub!");
    }

    protected int computeVerticalScrollExtent() {
        throw new RuntimeException("Stub!");
    }

    public void computeScroll() {
        throw new RuntimeException("Stub!");
    }

    public boolean onHoverEvent(MotionEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onTouchEvent(MotionEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onTrackballEvent(MotionEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        throw new RuntimeException("Stub!");
    }

    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public boolean shouldDelayChildPressedState() {
        throw new RuntimeException("Stub!");
    }

    public CharSequence getAccessibilityClassName() {
        throw new RuntimeException("Stub!");
    }

    public void onProvideVirtualStructure(ViewStructure structure) {
        throw new RuntimeException("Stub!");
    }

    public void onProvideAutofillVirtualStructure(ViewStructure structure, int flags) {
        throw new RuntimeException("Stub!");
    }

    public void onProvideContentCaptureStructure(@NonNull ViewStructure structure, int flags) {
        throw new RuntimeException("Stub!");
    }

    public void autofill(SparseArray<AutofillValue> values) {
        throw new RuntimeException("Stub!");
    }

    public boolean isVisibleToUserForAutofill(int virtualId) {
        throw new RuntimeException("Stub!");
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        throw new RuntimeException("Stub!");
    }

    protected void onWindowVisibilityChanged(int visibility) {
        throw new RuntimeException("Stub!");
    }

    protected void onDraw(Canvas canvas) {
        throw new RuntimeException("Stub!");
    }

    public boolean performLongClick() {
        throw new RuntimeException("Stub!");
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        throw new RuntimeException("Stub!");
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        throw new RuntimeException("Stub!");
    }

    public boolean onDragEvent(DragEvent event) {
        throw new RuntimeException("Stub!");
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        throw new RuntimeException("Stub!");
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        throw new RuntimeException("Stub!");
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        throw new RuntimeException("Stub!");
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        throw new RuntimeException("Stub!");
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        throw new RuntimeException("Stub!");
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        throw new RuntimeException("Stub!");
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        throw new RuntimeException("Stub!");
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        throw new RuntimeException("Stub!");
    }

    public void setBackgroundColor(int color) {
        throw new RuntimeException("Stub!");
    }

    public void setLayerType(int layerType, Paint paint) {
    }

    protected void dispatchDraw(Canvas canvas) {
        throw new RuntimeException("Stub!");
    }

    public void onStartTemporaryDetach() {
        throw new RuntimeException("Stub!");
    }

    public void onFinishTemporaryDetach() {
        throw new RuntimeException("Stub!");
    }

    public Handler getHandler() {
        throw new RuntimeException("Stub!");
    }

    public View findFocus() {
        throw new RuntimeException("Stub!");
    }

    @Nullable
    public static PackageInfo getCurrentWebViewPackage() {
        throw new RuntimeException("Stub!");
    }

    public boolean onCheckIsTextEditor() {
        throw new RuntimeException("Stub!");
    }

    public class WebViewTransport {
        public WebViewTransport() {
            throw new RuntimeException("Stub!");
        }

        public synchronized void setWebView(@Nullable WebView webview) {
            throw new RuntimeException("Stub!");
        }

        @Nullable
        public synchronized WebView getWebView() {
            throw new RuntimeException("Stub!");
        }
    }

    public abstract static class VisualStateCallback {
        public VisualStateCallback() {
            throw new RuntimeException("Stub!");
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

        HitTestResult() {
            throw new RuntimeException("Stub!");
        }

        public int getType() {
            throw new RuntimeException("Stub!");
        }

        @Nullable
        public String getExtra() {
            throw new RuntimeException("Stub!");
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
