package com.elite.pedometer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.elite.pedometer.util.PreferencesKeys;

/**
 * Created by dhaval.mehta on 21-Sep-2017.
 */

public class ActSplashScreen extends BaseActivity {

    //
    String TAG = "==ActSplashScreen==";
    String isSettingPageFirstTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try
        {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            super.onCreate(savedInstanceState);

            App.showLogTAG(TAG);
            setContentView(R.layout.act_splash_screen);

            //
            if (App.sharePrefrences.getStringPref(PreferencesKeys.isSettingPageFirstTime).equalsIgnoreCase(""))
                isSettingPageFirstTime = App.TRUE;
            else
                isSettingPageFirstTime = App.sharePrefrences.getStringPref(PreferencesKeys.isSettingPageFirstTime);
            App.showLog(TAG + "==isSettingPageFirstTime==" + isSettingPageFirstTime);

//            //
//            if (App.sharePrefrences.getStringPref(PreferencesKeys.isRunning).equalsIgnoreCase(""))
//                App.sharePrefrences.setPref(PreferencesKeys.isRunning, App.FALSE);

            //
            String todayStep = App.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps);
            App.showLog("==todayStep==" + todayStep);
            if (App.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps).equalsIgnoreCase(""))
                App.sharePrefrences.setPref(PreferencesKeys.strTodaysSteps, "0");


            //
            displaySplash();

        } catch (Exception e) {e.printStackTrace();}
    }


    private void displaySplash() {
        // TODO Auto-generated method stub b

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

                if (isSettingPageFirstTime.equalsIgnoreCase(App.TRUE))
                {
                    Intent iv = new Intent(ActSplashScreen.this, ActSettings.class);
                    iv.putExtra(App.ITAG_FROM, "ActSplashScreen");
                    App.myStartActivity(ActSplashScreen.this, iv);
                    finish();
                }
                else
                {
                    Intent iv = new Intent(ActSplashScreen.this, ActDashboard.class);
                    iv.putExtra(App.ITAG_FROM, "ActSplashScreen");
                    App.myStartActivity(ActSplashScreen.this, iv);
                    finish();
                }

            }
        }, App.SPLASH_DELAY_TIME);
    }
}
