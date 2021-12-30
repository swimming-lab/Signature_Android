package com.swimming.signature.bridge;

import android.webkit.JavascriptInterface;

import com.swimming.signature.BuildConfig;

public class AndroidBridge {
    private boolean isLayer = false;

    @JavascriptInterface
    public void setIsLayer(final boolean isLayer) {
        this.isLayer = isLayer;
    }

    public boolean isLayer() {
        return this.isLayer;
    }

    @JavascriptInterface
    public String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }
}
