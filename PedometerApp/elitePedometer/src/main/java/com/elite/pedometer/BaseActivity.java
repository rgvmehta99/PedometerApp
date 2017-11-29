package com.elite.pedometer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Created by dhaval.mehta on 21-Sep-2017.
 */

public class BaseActivity extends FragmentActivity {

    String TAG = "==BaseActivity==";

    FrameLayout baseFrame;
    protected LinearLayout llContainerMain, llContainerSub;
    ImageView ivAppLogo, ivHistory, ivSetting;
    TextView tvTitle;
    protected RelativeLayout rlBaseMainHeader;
    protected DrawerLayout drawer;
    protected App app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (App.blnFullscreenActvitity == true) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            App.blnFullscreenActvitity = false;
        }
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.act_baseactivity);

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            baseInitialization();
            setBaseClickEvents();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setBaseClickEvents() {
        try {
            llContainerSub.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // TODO Auto-generated method stub
                    System.out.println("===on touch=2=");
                    if (v instanceof EditText) {
                        System.out.println("=touch no hide edittext==2=");
                    } else {
                        System.out.println("===on touch hide=2=");
                        app.hideKeyBoard(v);
                    }
                    return true;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void baseInitialization() {
        try {
            app = (App) getApplicationContext();

            tvTitle = (TextView) findViewById(R.id.tvTitle);
            tvTitle.setTypeface(App.getTribuchet_MS());

            ivAppLogo = (ImageView) findViewById(R.id.ivAppLogo);
            ivHistory = (ImageView) findViewById(R.id.ivHistory);
            ivHistory.setVisibility(View.GONE);
            ivSetting = (ImageView) findViewById(R.id.ivSetting);
            ivSetting.setVisibility(View.GONE);

            baseFrame = (FrameLayout) findViewById(R.id.baseFrame);
            rlBaseMainHeader = (RelativeLayout) findViewById(R.id.rlBaseMainHeader);
            llContainerMain = (LinearLayout) findViewById(R.id.llContainerMain);
            llContainerSub = (LinearLayout) findViewById(R.id.llContainerSub);


            setBaseMenuDrawerDataAnyActivity();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    public void setBaseMenuDrawerDataAnyActivity() {
        try {
            App.showLog(TAG + "==setBaseMenuDrawerDataAnyActivity==");


        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    public void animStartActivity() {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    public void animFinishActivity() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    protected void onResume() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }



    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(llContainerMain.getWindowToken(), 0);
        super.onPause();
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        System.out.println("=====Base onStop====");
        //	setCloseDrawerMenu(true);
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        System.out.println("=====Base onDestroy====");
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
        } else {
            try {
                super.onBackPressed();
                animFinishActivity();
            } catch (Exception e) {
                e.printStackTrace();
                App.showLog("==Exception on base back click====");
            }
        }
    }


    public void showExitDialog() {

        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawers();
        }
        else
        {
            final Dialog dialog = new Dialog(BaseActivity.this);
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // for dialog shadow
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
            dialog.setContentView(R.layout.popup_exit);

            TextView tvExitMessage = (TextView) dialog.findViewById(R.id.tvMessage);
            TextView tvCancel = (TextView) dialog.findViewById(R.id.tvCancel);
            TextView tvOK = (TextView) dialog.findViewById(R.id.tvOk);

            tvExitMessage.setTypeface(App.getTribuchet_MS());
            tvCancel.setTypeface(App.getTribuchet_MS());
            tvOK.setTypeface(App.getTribuchet_MS());

            dialog.show();

            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    }, 180);
                }
            });

            tvOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            App.myFinishActivity(BaseActivity.this);
                            finishAffinity();
                            onBackPressed();
                            return;
                        }
                    }, 180);

                }
            });
        }
    }

}
