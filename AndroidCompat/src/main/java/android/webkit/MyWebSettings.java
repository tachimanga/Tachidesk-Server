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

import androidx.annotation.Nullable;

public class MyWebSettings extends WebSettings {
    public MyWebSettings() {
    }

    @Override
    public void setSupportZoom(boolean var1) {

    }

    @Override
    public boolean supportZoom() {
        return false;
    }

    @Override
    public void setMediaPlaybackRequiresUserGesture(boolean var1) {

    }

    @Override
    public boolean getMediaPlaybackRequiresUserGesture() {
        return false;
    }

    @Override
    public void setBuiltInZoomControls(boolean var1) {

    }

    @Override
    public boolean getBuiltInZoomControls() {
        return false;
    }

    @Override
    public void setDisplayZoomControls(boolean var1) {

    }

    @Override
    public boolean getDisplayZoomControls() {
        return false;
    }

    @Override
    public void setAllowFileAccess(boolean var1) {

    }

    @Override
    public boolean getAllowFileAccess() {
        return false;
    }

    @Override
    public void setAllowContentAccess(boolean var1) {

    }

    @Override
    public boolean getAllowContentAccess() {
        return false;
    }

    @Override
    public void setLoadWithOverviewMode(boolean var1) {

    }

    @Override
    public boolean getLoadWithOverviewMode() {
        return false;
    }

    @Override
    public void setEnableSmoothTransition(boolean var1) {

    }

    @Override
    public boolean enableSmoothTransition() {
        return false;
    }

    @Override
    public void setSaveFormData(boolean var1) {

    }

    @Override
    public boolean getSaveFormData() {
        return false;
    }

    @Override
    public void setSavePassword(boolean var1) {

    }

    @Override
    public boolean getSavePassword() {
        return false;
    }

    @Override
    public void setTextZoom(int var1) {

    }

    @Override
    public int getTextZoom() {
        return 0;
    }

    @Override
    public synchronized void setTextSize(TextSize t) {
    }

    @Override
    public synchronized TextSize getTextSize() {
        return TextSize.SMALLER;
    }

    @Override
    public void setDefaultZoom(ZoomDensity var1) {

    }

    @Override
    public ZoomDensity getDefaultZoom() {
        return null;
    }

    @Override
    public void setLightTouchEnabled(boolean var1) {

    }

    @Override
    public boolean getLightTouchEnabled() {
        return false;
    }

    @Override
    public void setUseWideViewPort(boolean var1) {

    }

    @Override
    public boolean getUseWideViewPort() {
        return false;
    }

    @Override
    public void setSupportMultipleWindows(boolean var1) {

    }

    @Override
    public boolean supportMultipleWindows() {
        return false;
    }

    @Override
    public void setLayoutAlgorithm(LayoutAlgorithm var1) {

    }

    @Override
    public LayoutAlgorithm getLayoutAlgorithm() {
        return null;
    }

    @Override
    public void setStandardFontFamily(String var1) {

    }

    @Override
    public String getStandardFontFamily() {
        return null;
    }

    @Override
    public void setFixedFontFamily(String var1) {

    }

    @Override
    public String getFixedFontFamily() {
        return null;
    }

    @Override
    public void setSansSerifFontFamily(String var1) {

    }

    @Override
    public String getSansSerifFontFamily() {
        return null;
    }

    @Override
    public void setSerifFontFamily(String var1) {

    }

    @Override
    public String getSerifFontFamily() {
        return null;
    }

    @Override
    public void setCursiveFontFamily(String var1) {

    }

    @Override
    public String getCursiveFontFamily() {
        return null;
    }

    @Override
    public void setFantasyFontFamily(String var1) {

    }

    @Override
    public String getFantasyFontFamily() {
        return null;
    }

    @Override
    public void setMinimumFontSize(int var1) {

    }

    @Override
    public int getMinimumFontSize() {
        return 0;
    }

    @Override
    public void setMinimumLogicalFontSize(int var1) {

    }

    @Override
    public int getMinimumLogicalFontSize() {
        return 0;
    }

    @Override
    public void setDefaultFontSize(int var1) {

    }

    @Override
    public int getDefaultFontSize() {
        return 0;
    }

    @Override
    public void setDefaultFixedFontSize(int var1) {

    }

    @Override
    public int getDefaultFixedFontSize() {
        return 0;
    }

    @Override
    public void setLoadsImagesAutomatically(boolean var1) {

    }

    @Override
    public boolean getLoadsImagesAutomatically() {
        return false;
    }

    @Override
    public void setBlockNetworkImage(boolean var1) {

    }

    @Override
    public boolean getBlockNetworkImage() {
        return false;
    }

    @Override
    public void setBlockNetworkLoads(boolean var1) {

    }

    @Override
    public boolean getBlockNetworkLoads() {
        return false;
    }

    @Override
    public void setJavaScriptEnabled(boolean var1) {

    }

    @Override
    public void setAllowUniversalAccessFromFileURLs(boolean var1) {

    }

    @Override
    public void setAllowFileAccessFromFileURLs(boolean var1) {

    }

    @Override
    public void setPluginState(PluginState var1) {

    }

    @Override
    public void setDatabasePath(String var1) {

    }

    @Override
    public void setGeolocationDatabasePath(String var1) {

    }

    @Override
    public void setAppCacheEnabled(boolean var1) {

    }

    @Override
    public void setAppCachePath(String var1) {

    }

    @Override
    public void setAppCacheMaxSize(long var1) {

    }

    @Override
    public void setDatabaseEnabled(boolean var1) {

    }

    @Override
    public void setDomStorageEnabled(boolean var1) {

    }

    @Override
    public boolean getDomStorageEnabled() {
        return false;
    }

    @Override
    public String getDatabasePath() {
        return null;
    }

    @Override
    public boolean getDatabaseEnabled() {
        return false;
    }

    @Override
    public void setGeolocationEnabled(boolean var1) {

    }

    @Override
    public boolean getJavaScriptEnabled() {
        return false;
    }

    @Override
    public boolean getAllowUniversalAccessFromFileURLs() {
        return false;
    }

    @Override
    public boolean getAllowFileAccessFromFileURLs() {
        return false;
    }

    @Override
    public PluginState getPluginState() {
        return null;
    }

    @Override
    public void setJavaScriptCanOpenWindowsAutomatically(boolean var1) {

    }

    @Override
    public boolean getJavaScriptCanOpenWindowsAutomatically() {
        return false;
    }

    @Override
    public void setDefaultTextEncodingName(String var1) {

    }

    @Override
    public String getDefaultTextEncodingName() {
        return null;
    }

    @Override
    public void setUserAgentString(@Nullable String var1) {

    }

    @Override
    public String getUserAgentString() {
        return null;
    }

    @Override
    public void setNeedInitialFocus(boolean var1) {

    }

    @Override
    public void setRenderPriority(RenderPriority var1) {

    }

    @Override
    public void setCacheMode(int var1) {

    }

    @Override
    public int getCacheMode() {
        return 0;
    }

    @Override
    public void setMixedContentMode(int var1) {

    }

    @Override
    public int getMixedContentMode() {
        return 0;
    }

    @Override
    public void setOffscreenPreRaster(boolean var1) {

    }

    @Override
    public boolean getOffscreenPreRaster() {
        return false;
    }

    @Override
    public void setSafeBrowsingEnabled(boolean var1) {

    }

    @Override
    public boolean getSafeBrowsingEnabled() {
        return false;
    }

    @Override
    public void setForceDark(int forceDark) {
    }

    @Override
    public int getForceDark() {
        return 0;
    }

    @Override
    public void setDisabledActionModeMenuItems(int var1) {

    }

    @Override
    public int getDisabledActionModeMenuItems() {
        return 0;
    }
}
