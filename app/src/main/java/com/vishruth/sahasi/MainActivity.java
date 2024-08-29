package com.vishruth.sahasi;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.net.MailTo;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private WebView webView;
    private static final String PREFS_NAME = "SahasiPrefs";
    private static final String FIRST_TIME_KEY = "firstTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.webView = findViewById(R.id.webView);

        // Check if it's the first time the app is opened
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean firstTime = preferences.getBoolean(FIRST_TIME_KEY, true);

        if (firstTime) {
            showFirstTimeMessage();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(FIRST_TIME_KEY, false);
            editor.apply();
        }

        WebSettings webSettings = this.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("sms:") || url.startsWith("tel:") || url.startsWith(MailTo.MAILTO_SCHEME)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                view.loadUrl(url);
                return false;
            }
        });
        this.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, LOCATION_PERMISSION_REQUEST_CODE);
                    return;
                }
                callback.invoke(origin, true, false);
            }
        });
        this.webView.loadUrl("https://wpwomensafety.netlify.app");
    }

    private void showFirstTimeMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Important Notice")
                .setMessage("Welcome to Sahasi! Please note that location services may not work properly during the first usage. If you encounter any issues after granting all required permissions, please restart the app. We're here to ensure your experience is smooth and reliable.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == 0) {
            this.webView.reload();
        }
    }

    @Override
    public void onBackPressed() {
        if (this.webView.canGoBack()) {
            this.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}