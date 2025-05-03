package android.webkit;

import android.net.WebAddress;
import androidx.annotation.Nullable;
import xyz.nulldev.androidcompat.CommonSwitch;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class McCookieManager extends CookieManager {

    private boolean acceptCookie = true;
    private boolean acceptThirdPartyCookies = true;
    private boolean allowFileSchemeCookies = false;

    @Override
    public void setAcceptCookie(boolean accept) {
        this.acceptCookie = accept;
    }

    @Override
    public boolean acceptCookie() {
        return this.acceptCookie;
    }

    @Override
    public void setAcceptThirdPartyCookies(WebView webview, boolean accept) {
        this.acceptThirdPartyCookies = accept;
    }

    @Override
    public boolean acceptThirdPartyCookies(WebView webview) {
        return this.acceptThirdPartyCookies;
    }

    @Override
    public void setCookie(String url, String value) {
        System.out.println("[Cookies]setCookie url:" + url + ", value:" + value + ", enable:" + CommonSwitch.ENABLE_NATIVE_COOKIE);
        if (!CommonSwitch.ENABLE_NATIVE_COOKIE) {
            return;
        }
        if (url == null) {
            return;
        }
        if (value == null || value.isEmpty()) {
            return;
        }
        if (!url.toLowerCase(Locale.ROOT).startsWith("http")) {
            url = "http://" + url;
        }
        try {
            nativeSetCookie(url, value);
        } catch (Exception e) {
            System.out.println("[Cookies]nativeSetCookie e:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void setCookie(String url, String value, @Nullable ValueCallback<Boolean> callback) {
        setCookie(url, value);
        if (callback != null) {
            callback.onReceiveValue(true);
        }
    }

    @Override
    public String getCookie(String url) {
        System.out.println("[Cookies]getCookie url:" + url + ", enable:" + CommonSwitch.ENABLE_NATIVE_COOKIE);
        if (!CommonSwitch.ENABLE_NATIVE_COOKIE) {
            return "";
        }
        if (url == null) {
            return null;
        }
        if (!url.toLowerCase(Locale.ROOT).startsWith("http")) {
            url = "http://" + url;
        }
        try {
            String cookie = nativeGetCookie(url);
            // Return null if the string is empty to match legacy behavior
            return cookie == null || cookie.isEmpty() ? null : cookie;
        } catch (Exception e) {
            System.out.println("[Cookies]nativeGetCookie e:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getCookie(String url, boolean privateBrowsing) {
        return getCookie(url);
    }

    @Override
    public synchronized String getCookie(WebAddress uri) {
        return getCookie(uri.toString());
    }

    @Override
    public void removeSessionCookie() {
    }

    @Override
    public void removeSessionCookies(@Nullable ValueCallback<Boolean> callback) {
    }

    @Override
    public void removeAllCookie() {
    }

    @Override
    public void removeAllCookies(@Nullable ValueCallback<Boolean> callback) {
    }

    @Override
    public boolean hasCookies() {
        return true;
    }

    @Override
    public boolean hasCookies(boolean privateBrowsing) {
        return true;
    }

    @Override
    public void removeExpiredCookie() {
    }

    @Override
    public void flush() {
    }

    @Override
    protected boolean allowFileSchemeCookiesImpl() {
        return this.allowFileSchemeCookies;
    }

    @Override
    protected void setAcceptFileSchemeCookiesImpl(boolean accept) {
        this.allowFileSchemeCookies = accept;
    }

    private static String nativeGetCookie(String url) {
        byte[] bytes = nativeGetCookie0(stringToUtf8ByteArray(url));
        return utf8ByteBufferToString(bytes);
    }

    private static void nativeSetCookie(String url, String value) {
        nativeSetCookie0(stringToUtf8ByteArray(url), stringToUtf8ByteArray(value));
    }

    private static byte[] stringToUtf8ByteArray(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }

    private static String utf8ByteBufferToString(byte[] buffer) {
        if (buffer == null) {
            return null;
        }
        return new String(buffer, StandardCharsets.UTF_8);
    }

    private static native byte[] nativeGetCookie0(byte[] urlUtf8);
    private static native void nativeSetCookie0(byte[] urlUtf8, byte[] valueUtf8);
}
