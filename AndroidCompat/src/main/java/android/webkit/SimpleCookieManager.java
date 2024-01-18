package android.webkit;

import android.net.WebAddress;
import androidx.annotation.Nullable;

/**
 * @author jichao.wjc
 * Created by Yifeng on 2023/12/4
 */
public class SimpleCookieManager extends CookieManager{
    @Override
    public void setAcceptCookie(boolean accept) {

    }

    @Override
    public boolean acceptCookie() {
        return true;
    }

    @Override
    public void setAcceptThirdPartyCookies(WebView webview, boolean accept) {

    }

    @Override
    public boolean acceptThirdPartyCookies(WebView webview) {
        return true;
    }

    @Override
    public void setCookie(String url, String value) {
        System.out.println("[Cookies]setCookie url:" + url + ", value:" + value);
    }

    @Override
    public void setCookie(String url, String value, @Nullable ValueCallback<Boolean> callback) {

    }

    @Override
    public String getCookie(String url) {
        System.out.println("[Cookies]getCookie url:" + url);
        return "";
    }

    @Override
    public String getCookie(String url, boolean privateBrowsing) {
        return null;
    }

    @Override
    public synchronized String getCookie(WebAddress uri) {
        return super.getCookie(uri);
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
        return true;
    }

    @Override
    protected void setAcceptFileSchemeCookiesImpl(boolean accept) {

    }
}
