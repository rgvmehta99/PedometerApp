package com.elite.pedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by dhaval.mehta on 22-Sep-2017.
 */

public class StepTrackerRebootBroadcastService extends BroadcastReceiver {

    String TAG = "==StepTrackerRebootBroadcastService==";

    @Override
    public void onReceive(Context context, Intent intent) {

        App.showLog(TAG + "==[[onReceive]]==[[--BroadcastReceiver--]]");

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {

            App.showLog(TAG + "==[[onReceive]]==[[--BroadcastReceiver--]]==[[--ACTION_BOOT_COMPLETED--]]");
            // App.showToastShort(context, "Step Service Starts Again..");
            Intent serviceIntent = new Intent(context, StepTrackerService.class);
            context.startService(serviceIntent);
        }

    }
}
