package com.uchur.uchur;

import androidx.annotation.NonNull;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private RelativeLayout splashScreen;
    private String webUrl = "https://uchur.ru/?s_layout=20";
    private Context c;
    private BroadcastReceiver broadcastReceiver;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
//    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().subscribeToTopic("notifications");

        //Settings up Web View

        webView = (WebView) findViewById(R.id.webView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        splashScreen = (RelativeLayout) findViewById(R.id.splashScreen);

//        bottomNavigationView = (BottomNavigationView) findViewById(R.id.replaced_navigation_bar);
//        bottomNavigationView.setSelectedItemId(R.id.shorts);
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.cart:
//                        return true;
//                    case R.id.shorts:
//                        return true;
//                    case R.id.wishlist:
//                        return true;
//                    case R.id.profile:
//                        return true;
//                }
//                return false;
//            }
//        });

        webView.loadUrl(webUrl);

        //Setting up web settings

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);

//        new MyAsyncTask().execute();

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
//                        alert.show();
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
//            String sticky_panel_script = "javascript:(function() { var sticky_panel=document.getElementsByClassName('ut2-sticky-panel__wrap');sticky_panel[0].style.display='none';})()";
//            webView.loadUrl(sticky_panel_script);
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getHost().contains("uchur.ru")) {
                    return false;
                }   else if (Uri.parse(url).getHost().contains("sberbank.ru")) {
                    return false;
                }
                else {
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

//                webView.loadUrl("javascript:(function() {" + "document.getElementsByClassName('ut2-sticky-panel__wrap')[0].style.display='none';})()");

                if (newProgress == 100) {
                    splashScreen.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    private class JSBridge {
        @JavascriptInterface
        public void calledFromJS() {

        }
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Document> {
        @Override
        protected Document doInBackground(Void... voids) {
            Document document = null;
            try {
                document = Jsoup.connect(webUrl).get();
//                document.getElementsByClass("ut2-sticky-panel__wrap").remove();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return document;
        }

        @Override
        protected void onPostExecute(Document document) {
            super.onPostExecute(document);
            webView.loadDataWithBaseURL(webUrl, document.toString(), "text/html", "utf-8", "");
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        swipeRefreshLayout.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
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