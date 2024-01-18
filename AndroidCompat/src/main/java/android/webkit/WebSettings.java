//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.webkit;

import android.annotation.Nullable;
import android.content.Context;

public abstract class WebSettings {
    public static final int FORCE_DARK_AUTO = 1;
    public static final int FORCE_DARK_OFF = 0;
    public static final int FORCE_DARK_ON = 2;
    public static final int LOAD_CACHE_ELSE_NETWORK = 1;
    public static final int LOAD_CACHE_ONLY = 3;
    public static final int LOAD_DEFAULT = -1;
    /** @deprecated */
    @Deprecated
    public static final int LOAD_NORMAL = 0;
    public static final int LOAD_NO_CACHE = 2;
    public static final int MENU_ITEM_NONE = 0;
    public static final int MENU_ITEM_PROCESS_TEXT = 4;
    public static final int MENU_ITEM_SHARE = 1;
    public static final int MENU_ITEM_WEB_SEARCH = 2;
    public static final int MIXED_CONTENT_ALWAYS_ALLOW = 0;
    public static final int MIXED_CONTENT_COMPATIBILITY_MODE = 2;
    public static final int MIXED_CONTENT_NEVER_ALLOW = 1;

    public WebSettings() {
    }

    public abstract void setSupportZoom(boolean var1);

    public abstract boolean supportZoom();

    public abstract void setMediaPlaybackRequiresUserGesture(boolean var1);

    public abstract boolean getMediaPlaybackRequiresUserGesture();

    public abstract void setBuiltInZoomControls(boolean var1);

    public abstract boolean getBuiltInZoomControls();

    public abstract void setDisplayZoomControls(boolean var1);

    public abstract boolean getDisplayZoomControls();

    public abstract void setAllowFileAccess(boolean var1);

    public abstract boolean getAllowFileAccess();

    public abstract void setAllowContentAccess(boolean var1);

    public abstract boolean getAllowContentAccess();

    public abstract void setLoadWithOverviewMode(boolean var1);

    public abstract boolean getLoadWithOverviewMode();

    /** @deprecated */
    @Deprecated
    public abstract void setEnableSmoothTransition(boolean var1);

    /** @deprecated */
    @Deprecated
    public abstract boolean enableSmoothTransition();

    /** @deprecated */
    @Deprecated
    public abstract void setSaveFormData(boolean var1);

    /** @deprecated */
    @Deprecated
    public abstract boolean getSaveFormData();

    /** @deprecated */
    @Deprecated
    public abstract void setSavePassword(boolean var1);

    /** @deprecated */
    @Deprecated
    public abstract boolean getSavePassword();

    public abstract void setTextZoom(int var1);

    public abstract int getTextZoom();

    /** @deprecated */
    @Deprecated
    public synchronized void setTextSize(TextSize t) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public synchronized TextSize getTextSize() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public abstract void setDefaultZoom(ZoomDensity var1);

    /** @deprecated */
    @Deprecated
    public abstract ZoomDensity getDefaultZoom();

    /** @deprecated */
    @Deprecated
    public abstract void setLightTouchEnabled(boolean var1);

    /** @deprecated */
    @Deprecated
    public abstract boolean getLightTouchEnabled();

    public abstract void setUseWideViewPort(boolean var1);

    public abstract boolean getUseWideViewPort();

    public abstract void setSupportMultipleWindows(boolean var1);

    public abstract boolean supportMultipleWindows();

    public abstract void setLayoutAlgorithm(LayoutAlgorithm var1);

    public abstract LayoutAlgorithm getLayoutAlgorithm();

    public abstract void setStandardFontFamily(String var1);

    public abstract String getStandardFontFamily();

    public abstract void setFixedFontFamily(String var1);

    public abstract String getFixedFontFamily();

    public abstract void setSansSerifFontFamily(String var1);

    public abstract String getSansSerifFontFamily();

    public abstract void setSerifFontFamily(String var1);

    public abstract String getSerifFontFamily();

    public abstract void setCursiveFontFamily(String var1);

    public abstract String getCursiveFontFamily();

    public abstract void setFantasyFontFamily(String var1);

    public abstract String getFantasyFontFamily();

    public abstract void setMinimumFontSize(int var1);

    public abstract int getMinimumFontSize();

    public abstract void setMinimumLogicalFontSize(int var1);

    public abstract int getMinimumLogicalFontSize();

    public abstract void setDefaultFontSize(int var1);

    public abstract int getDefaultFontSize();

    public abstract void setDefaultFixedFontSize(int var1);

    public abstract int getDefaultFixedFontSize();

    public abstract void setLoadsImagesAutomatically(boolean var1);

    public abstract boolean getLoadsImagesAutomatically();

    public abstract void setBlockNetworkImage(boolean var1);

    public abstract boolean getBlockNetworkImage();

    public abstract void setBlockNetworkLoads(boolean var1);

    public abstract boolean getBlockNetworkLoads();

    public abstract void setJavaScriptEnabled(boolean var1);

    /** @deprecated */
    @Deprecated
    public abstract void setAllowUniversalAccessFromFileURLs(boolean var1);

    /** @deprecated */
    @Deprecated
    public abstract void setAllowFileAccessFromFileURLs(boolean var1);

    /** @deprecated */
    @Deprecated
    public abstract void setPluginState(PluginState var1);

    /** @deprecated */
    @Deprecated
    public abstract void setDatabasePath(String var1);

    /** @deprecated */
    @Deprecated
    public abstract void setGeolocationDatabasePath(String var1);

    /** @deprecated */
    @Deprecated
    public abstract void setAppCacheEnabled(boolean var1);

    /** @deprecated */
    @Deprecated
    public abstract void setAppCachePath(String var1);

    /** @deprecated */
    @Deprecated
    public abstract void setAppCacheMaxSize(long var1);

    public abstract void setDatabaseEnabled(boolean var1);

    public abstract void setDomStorageEnabled(boolean var1);

    public abstract boolean getDomStorageEnabled();

    /** @deprecated */
    @Deprecated
    public abstract String getDatabasePath();

    public abstract boolean getDatabaseEnabled();

    public abstract void setGeolocationEnabled(boolean var1);

    public abstract boolean getJavaScriptEnabled();

    public abstract boolean getAllowUniversalAccessFromFileURLs();

    public abstract boolean getAllowFileAccessFromFileURLs();

    /** @deprecated */
    @Deprecated
    public abstract PluginState getPluginState();

    public abstract void setJavaScriptCanOpenWindowsAutomatically(boolean var1);

    public abstract boolean getJavaScriptCanOpenWindowsAutomatically();

    public abstract void setDefaultTextEncodingName(String var1);

    public abstract String getDefaultTextEncodingName();

    public abstract void setUserAgentString(@Nullable String var1);

    public abstract String getUserAgentString();

    public static String getDefaultUserAgent(Context context) {
        throw new RuntimeException("Stub!");
    }

    public abstract void setNeedInitialFocus(boolean var1);

    /** @deprecated */
    @Deprecated
    public abstract void setRenderPriority(RenderPriority var1);

    public abstract void setCacheMode(int var1);

    public abstract int getCacheMode();

    public abstract void setMixedContentMode(int var1);

    public abstract int getMixedContentMode();

    public abstract void setOffscreenPreRaster(boolean var1);

    public abstract boolean getOffscreenPreRaster();

    public abstract void setSafeBrowsingEnabled(boolean var1);

    public abstract boolean getSafeBrowsingEnabled();

    public void setForceDark(int forceDark) {
        throw new RuntimeException("Stub!");
    }

    public int getForceDark() {
        throw new RuntimeException("Stub!");
    }

    public abstract void setDisabledActionModeMenuItems(int var1);

    public abstract int getDisabledActionModeMenuItems();

    public static enum ZoomDensity {
        FAR,
        MEDIUM,
        CLOSE;

        private ZoomDensity() {
        }
    }

    /** @deprecated */
    @Deprecated
    public static enum TextSize {
        /** @deprecated */
        @Deprecated
        SMALLEST,
        /** @deprecated */
        @Deprecated
        SMALLER,
        /** @deprecated */
        @Deprecated
        NORMAL,
        /** @deprecated */
        @Deprecated
        LARGER,
        /** @deprecated */
        @Deprecated
        LARGEST;

        private TextSize() {
        }
    }

    public static enum RenderPriority {
        NORMAL,
        HIGH,
        LOW;

        private RenderPriority() {
        }
    }

    public static enum PluginState {
        ON,
        ON_DEMAND,
        OFF;

        private PluginState() {
        }
    }

    public static enum LayoutAlgorithm {
        NORMAL,
        /** @deprecated */
        @Deprecated
        SINGLE_COLUMN,
        /** @deprecated */
        @Deprecated
        NARROW_COLUMNS,
        TEXT_AUTOSIZING;

        private LayoutAlgorithm() {
        }
    }
}
