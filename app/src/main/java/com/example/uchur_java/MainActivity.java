package com.example.uchur_java;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private Activity activity = null;
    private RelativeLayout splashScreen;
    private RelativeLayout offlineScreen;
    private String webUrl = "https://uchur.ru/?s_layout=23";
    private WifiManager wifiManager;
    private Context c;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Settings up Web View

        webView = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        splashScreen = (RelativeLayout) findViewById(R.id.relativeLayout);
        offlineScreen = (RelativeLayout) findViewById(R.id.offlineScreen);
        webView.loadUrl(webUrl);

        //Setting up web settings

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);

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

//        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        ConnectivityManager connectivityManager = (ConnectivityManager)
//                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetInfo =
//                Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
//        NetworkInfo mobNetInfo =
//                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//        if (activeNetInfo != null) {
//            Toast.makeText(getApplicationContext(), "Active Network Type : " +
//                    activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
//        }
//        if (mobNetInfo != null) {
//            Toast.makeText(getApplicationContext(), "Mobile Network Type : " +
//                    mobNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
//        }

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
            offlineScreen.setVisibility(View.GONE);
        } else if(mobileNetwork.isConnected()) {
            splashScreen.setVisibility(View.VISIBLE);
            webView.setVisibility(View.VISIBLE);
            offlineScreen.setVisibility(View.GONE);
        } else {
            splashScreen.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
            offlineScreen.setVisibility(View.VISIBLE);
        }
////        if (wifi.isConnected()) {
////            splashScreen.setVisibility(View.GONE);
////        } else if(mobileNetwork.isConnected()) {
////            splashScreen.setVisibility(View.GONE);
////        } else {
////            splashScreen.showContextMenu();
////        }
    }

//    public class ConnectionChangeReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
//            NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//            NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//            if (activeNetInfo != null) {
//                Toast.makeText(context, "Active Network Type: " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
//            }
//            if (mobNetInfo != null) {
//                Toast.makeText(context, "Mobile Network Type : " + mobNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
//            }
//            if (wifiNetInfo != null) {
//                Toast.makeText(context, "WiFi Network Type: " + wifiNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    public boolean isOnline() {
//        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        // are we connected to the net(wifi or phone)
//        if ( cm.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
//                //cm.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
//                //cm.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING ||
//                cm.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED ) {
//            Log.e("Testing Internet Connection", "We have internet");
//            return true;
//
//        } else if (cm.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
//                ||  cm.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED){
//            showNoInternetConnectionDialog();
//            Log.e("Testing Internet Connection", "We dont have internet");
//            return false;
//        }
//        return false;
//
//    }
//    //Showing the No internet connection Custom Dialog =)
//    public void showNoInternetConnectionDialog(){
//        Log.e("Testing Internet Connection", "Entering showNoInternetConnectionDialog Method");
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Whoops! Its seems you don't have internet connection, please try again later!")
//                .setTitle("No Internet Connection")
//                .setCancelable(false)
//                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
//                        finish();
//                    }
//                });
//        final AlertDialog alert = builder.create();
//        alert.show();
//        Log.e("Testing Internet Connection", "Showed NoIntenetConnectionDialog");
//    }

//    public void SwitchWIFION(View view) {
//        Objects.requireNonNull(wifiManager).setWifiEnabled(true);
//        Toast.makeText(getApplicationContext(), "WIFI SWITCHED ON",
//                Toast.LENGTH_SHORT).show();
//    }
//    public void SwitchWIFIOFF(View view) {
//        Objects.requireNonNull(wifiManager).setWifiEnabled(false);
//        Toast.makeText(getApplicationContext(), "WIFI SWITCHED OFF",
//                Toast.LENGTH_SHORT).show();
//    }

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