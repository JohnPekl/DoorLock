package gist.mlv.doorlock;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

public class DeviceWebView extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_view);

        WebView deviceWebView = findViewById(R.id.device_webview);
        WebSettings webSettings = deviceWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String connect = getIntent().getStringExtra("EXTRA_SESSION_ID");

        deviceWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                handler.proceed("admin", "admin");
            }
        });
        deviceWebView.loadUrl(connect);
    }
}
