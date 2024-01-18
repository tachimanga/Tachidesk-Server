/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.webkit;

import android.net.WebAddress;
import androidx.annotation.Nullable;

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
