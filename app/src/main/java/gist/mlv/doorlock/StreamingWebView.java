package gist.mlv.doorlock;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

public class StreamingWebView extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.device_view);

        WebView deviceWebView = findViewById(R.id.device_webview);
        WebSettings webSettings = deviceWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String connect = "http://mlv.co.kr/ict/showvid.php?devid=%27000-000-000%27&key=%273030%27";
                //getIntent().getStringExtra("EXTRA_SESSION_ID");

        deviceWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                //handler.proceed("admin", "admin");
            }
        });
        deviceWebView.loadUrl(connect);
    }
}
