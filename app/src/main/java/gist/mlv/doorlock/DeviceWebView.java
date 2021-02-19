package gist.mlv.doorlock;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

import androidx.annotation.Nullable;

public class DeviceWebView extends Activity {
    public static String INTENT_EXTRA = "DeviceWebView_Extra";
    private WebView mWebView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_view);

        mWebView = findViewById(R.id.device_webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String json = getIntent().getStringExtra(INTENT_EXTRA);
        Device connect= (new Gson()).fromJson(json, Device.class);

        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                handler.proceed("admin", "admin");
            }
        });
        mWebView.loadUrl(connect.getUrlLocal());
    }
}
