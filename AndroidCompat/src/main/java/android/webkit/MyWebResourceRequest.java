package android.webkit;

import android.net.Uri;

import java.util.Map;

public class MyWebResourceRequest implements WebResourceRequest {

    private final String url;
    private final String method;
    private final Map<String, String> headers;

    public MyWebResourceRequest(String url, String method, Map<String, String> headers) {
        this.url = url;
        this.method = method;
        this.headers = headers;
    }

    @Override
    public Uri getUrl() {
        return Uri.parse(url);
    }

    @Override
    public boolean isForMainFrame() {
        return false;
    }

    @Override
    public boolean isRedirect() {
        return false;
    }

    @Override
    public boolean hasGesture() {
        return false;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        return headers;
    }
}
