package com.uchur.uchur;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

//    protected List<NetworkStateRecevierListener> listeners;
//
//    public NetworkStateRecevier() {
//        listeners = new ArrayList<>();
//        connected = null;
//    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = MainActivity.NetworkUtil.getConnectivityStatusString(context);
        Log.e("Sulod network reciever", "Sulod sa network reciever");
//        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
//            if (status == MainActivity.NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
//                new ForceExitPause(context).execute();
//            } else {
//                new ResumeForceExitPause(context).execute();
//            }
//        }
    }
}
