package com.elite.pedometer;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.elite.pedometer.AppWear;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

/**
 * Created by dhaval.mehta on 03-Oct-2017.
 */

public class MessageListener extends WearableListenerService {

    String TAG = "==MessageListener==";

    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        AppWear.showLogTAG(TAG + "==onMessageReceived==MessageEventPath==" + messageEvent.getPath());
        String msg = "";

        if (messageEvent.getPath().equals("/message_path"))
        {
            final String message = new String(messageEvent.getData());
            AppWear.showLog(TAG + "==Path Received==IF==" + messageEvent.getPath());
            AppWear.showLog(TAG + "==Message Received==IF==" + message);
            msg = message;
        }
        else
        {
            super.onMessageReceived(messageEvent);
        }


        Intent messageIntent = new Intent();
        messageIntent.setAction(Intent.ACTION_SEND);
        messageIntent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
    }

}
