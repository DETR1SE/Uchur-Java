package com.example.uchur_java;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private Activity activity = null;
    private RelativeLayout splashScreen;
    private RelativeLayout offlineScreen;
    private String webUrl = "https://uchur.ru/?s_layout=23";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Settings up Web View

        webView = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        webView.loadUrl(webUrl);
        splashScreen = (RelativeLayout) findViewById(R.id.relativeLayout);
        offlineScreen = (RelativeLayout) findViewById(R.id.offlineScreen);

        //Setting up web settings

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);

        checkConnection();

        webView.setWebViewClient(new UchurWebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getHost().contains("uchur.ru")) {
                    return false;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    view.getContext().startActivity(intent);
                    return true;
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);

                if (newProgress == 100) {
                    splashScreen.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    public class UchurWebViewClient extends WebViewClient {

    }

    public void checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()) {
            webView.setVisibility(View.VISIBLE);
            offlineScreen.setVisibility(View.INVISIBLE);
        } else if(mobileNetwork.isConnected()) {
            webView.setVisibility(View.VISIBLE);
            offlineScreen.setVisibility(View.INVISIBLE);
        } else {
            webView.setVisibility(View.INVISIBLE);
            offlineScreen.setVisibility(View.VISIBLE);
        }
//        if (wifi.isConnected()) {
//            splashScreen.setVisibility(View.GONE);
//        } else if(mobileNetwork.isConnected()) {
//            splashScreen.setVisibility(View.GONE);
//        } else {
//            splashScreen.showContextMenu();
//        }
    }

//    Back button

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        }
        else {
            super.onBackPressed();
        }
    }
}