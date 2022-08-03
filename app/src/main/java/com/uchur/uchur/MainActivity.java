package com.uchur.uchur;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private RelativeLayout splashScreen;
    private String webUrl = "https://uchur.ru/?s_layout=23";
    private Context c;
    private BroadcastReceiver broadcastReceiver;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Settings up Web View

        webView = (WebView) findViewById(R.id.webView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        splashScreen = (RelativeLayout) findViewById(R.id.splashScreen);
        webView.loadUrl(webUrl);

        //Setting up web settings

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.loadUrl(webView.getUrl());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        checkConnection();

        c = MainActivity.this;
        broadcastReceiver = new BroadcastReceiver() {

            AlertDialog.Builder builder;

            @Override
            public void onReceive(Context context, Intent intent) {

                ConnectivityManager connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo activeNetWifi = connectivityManager
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                boolean isConnectedMobile = activeNetInfo != null
                        && activeNetInfo.isConnectedOrConnecting();
                boolean isConnectedWifi = activeNetWifi != null
                        && activeNetWifi.isConnectedOrConnecting();
                AlertDialog alert = alertNoNetwork();
                if (isConnectedMobile || isConnectedWifi) {
                    if (alert != null && alert.isShowing()) {
                        alert.dismiss();
                    }
                } else {
                    if (alert != null && !alert.isShowing()) {
                        switchToOfflineActivity();
                        alert.show();
                    }
                }
            }

            public AlertDialog alertNoNetwork() {
                builder = new AlertDialog.Builder(c);
                builder.setMessage(R.string.err_network_failure_title)
                        .setMessage(R.string.err_network_failure_message)
                        .setCancelable(false)
                        .setPositiveButton("Quit",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                return builder.create();
            }
        };

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

    @Override
    public void onStart() {
        super.onStart();

        swipeRefreshLayout.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener =
        new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (webView.getScrollY() == 0)
                    swipeRefreshLayout.setEnabled(true);
                else
                    swipeRefreshLayout.setEnabled(false);
            }
        });
    }

    public void onStop() {
        swipeRefreshLayout.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
        super.onStop();
    }

    private void switchToMainActivity() {
        Intent switchActivityToMainIntent = new Intent(this, MainActivity.class);
        startActivity(switchActivityToMainIntent);
    }

    private void switchToOfflineActivity() {
        Intent switchActivityIntent = new Intent(this, offlineScreen.class);
        startActivity(switchActivityIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public static class NetworkUtil {
        public static final int TYPE_WIFI = 1;
        public static final int TYPE_MOBILE = 2;
        public static final int TYPE_NOT_CONNECTED = 0;
        public static final int NETWORK_STATUS_NOT_CONNECTED = 0;
        public static final int NETWORK_STATUS_WIFI = 1;
        public static final int NETWORK_STATUS_MOBILE = 2;

        public static int getConnectivityStatus(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (null != activeNetwork) {
                if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                    return TYPE_WIFI;

                if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                    return TYPE_MOBILE;
            }
            return TYPE_NOT_CONNECTED;
        }

        public static int getConnectivityStatusString(Context context) {
            int conn = NetworkUtil.getConnectivityStatus(context);
            int status = 0;
            if (conn == NetworkUtil.TYPE_WIFI) {
                status = NETWORK_STATUS_WIFI;
            } else if (conn == NetworkUtil.TYPE_MOBILE) {
                status = NETWORK_STATUS_MOBILE;
            } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
                status = NETWORK_STATUS_NOT_CONNECTED;
            }
            return status;
        }
    }

    public class UchurWebViewClient extends WebViewClient {

    }

    public void checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()) {
            webView.setVisibility(View.VISIBLE);
            splashScreen.setVisibility(View.VISIBLE);
        } else if(mobileNetwork.isConnected()) {
            splashScreen.setVisibility(View.VISIBLE);
            webView.setVisibility(View.VISIBLE);
        } else {
            splashScreen.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
        }
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