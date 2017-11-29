package com.elite.pedometer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.elite.pedometer.util.DatabaseHelper;
import com.elite.pedometer.util.PreferencesKeys;

/**
 * Created by dhaval.mehta on 25-Sep-2017.
 */

public class ActSettings extends BaseActivity {

    String TAG = "==ActSettings==";

    TextView tvTargetSetps, /*tvBodyWeight,*/ tvSkip, tvSet/*, tvTagSelectWalkMode*/;
    EditText etTargetSteps/*, etBodyWeight*/;
//    RadioGroup rdGrpSelectWalkMode;
//    RadioButton rdBtnWalking, rdBtnRunning;

    String strFrom = "";
    boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try
        {
            super.onCreate(savedInstanceState);
            App.showLogTAG(TAG);
            ViewGroup.inflate(this, R.layout.act_settings, llContainerSub);

            getIntentData();
            initialisation();
            setFonts();
            setClickEvents();

            App.showLog(TAG + "==target==steps==db==" + App.getDatabaseHelper().getTargetStepsDateWise(App.getCurrentDate()));

        } catch (Exception e) {e.printStackTrace();}
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (etTargetSteps != null)
        {
            if (App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps) != null &&
                    App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps).length() > 0)
            {
                etTargetSteps.setText(App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps));
                etTargetSteps.setSelection(App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps).length());
            }
        }


//        if (etBodyWeight != null)
//        {
//            if (App.sharePrefrences.getStringPref(PreferencesKeys.strWeight) != null &&
//                    App.sharePrefrences.getStringPref(PreferencesKeys.strWeight).length() > 0)
//            {
//                etBodyWeight.setText(App.sharePrefrences.getStringPref(PreferencesKeys.strWeight));
//                etBodyWeight.setSelection(App.sharePrefrences.getStringPref(PreferencesKeys.strWeight).length());
//            }
//        }
//
//        if (rdBtnRunning != null && rdBtnWalking != null)
//        {
//            if (App.sharePrefrences.getStringPref(PreferencesKeys.isRunning) != null &&
//                    App.sharePrefrences.getStringPref(PreferencesKeys.isRunning).length() > 0)
//            {
//                if (App.sharePrefrences.getStringPref(PreferencesKeys.isRunning).equalsIgnoreCase(App.TRUE)) {
//                    rdBtnRunning.setChecked(true);
//                    rdBtnWalking.setChecked(false);
//                } else{
//                    rdBtnWalking.setChecked(true);
//                    rdBtnRunning.setChecked(false);
//                }
//            }
//
//        }
    }


    private void getIntentData() {
        try {

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                //
                if (bundle.getString(App.ITAG_FROM) != null &&
                        bundle.getString(App.ITAG_FROM).length() > 0) {
                    strFrom = bundle.getString(App.ITAG_FROM);
                    App.showLog(TAG + "==strFrom==" + strFrom);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initialisation() {
        try
        {
            //
            tvTitle.setText(App.TITLE_SETTINGS);
            if (!strFrom.equalsIgnoreCase("ActSplashScreen")) {
                ivAppLogo.setImageResource(R.drawable.ic_back);
            }

            tvTargetSetps = (TextView) findViewById(R.id.tvTargetSetps);
//            tvBodyWeight = (TextView) findViewById(R.id.tvBodyWeight);
//            tvTagSelectWalkMode = (TextView) findViewById(R.id.tvTagSelectWalkMode);
            tvSet = (TextView) findViewById(R.id.tvSet);
            tvSkip = (TextView) findViewById(R.id.tvSkip);
            etTargetSteps = (EditText) findViewById(R.id.etTargetSteps);
//            etBodyWeight = (EditText) findViewById(R.id.etBodyWeight);
//
//            rdGrpSelectWalkMode = (RadioGroup) findViewById(R.id.rdGrpSelectWalkMode);
//            rdBtnWalking = (RadioButton) findViewById(R.id.rdBtnWalking);
//            rdBtnRunning = (RadioButton) findViewById(R.id.rdBtnRunning);

            //
            if (strFrom != null && !strFrom.equalsIgnoreCase("ActSplashScreen"))
            {
                tvSkip.setVisibility(View.GONE);
            }

        } catch (Exception e) {e.printStackTrace();}
    }


    private void setFonts() {
        try
        {
            tvTargetSetps.setTypeface(App.getTribuchet_MS());
//            tvBodyWeight.setTypeface(App.getTribuchet_MS());
//            tvTagSelectWalkMode.setTypeface(App.getTribuchet_MS());
            tvSet.setTypeface(App.getTribuchet_MS());
            tvSkip.setTypeface(App.getTribuchet_MS());
            etTargetSteps.setTypeface(App.getTribuchet_MS());
//            etBodyWeight.setTypeface(App.getTribuchet_MS());
//
//            rdBtnWalking.setTypeface(App.getTribuchet_MS());
//            rdBtnRunning.setTypeface(App.getTribuchet_MS());
        } catch (Exception e) {e.printStackTrace();}
    }


    private void setClickEvents() {
        try
        {
            //
            ivAppLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });


            //
            etTargetSteps.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        App.hideSoftKeyboardMy(ActSettings.this);
                        tvSet.performClick();
                        return true;
                    }
                    return false;
                }
            });


            //
//            etBodyWeight.setOnEditorActionListener(new EditText.OnEditorActionListener() {
//                @Override
//                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                    if (actionId == EditorInfo.IME_ACTION_DONE) {
//                        App.hideSoftKeyboardMy(ActSettings.this);
//                        tvSet.performClick();
//                        return true;
//                    }
//                    return false;
//                }
//            });


//            //
//            rdGrpSelectWalkMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                public void onCheckedChanged(RadioGroup group, int checkedId) {
//
//                    if(rdBtnWalking.isChecked())
//                    {
//                        isRunning = false;
//                        App.showLog(TAG + "==isRunning==" + isRunning);
//
//                    }
//                    else if(rdBtnRunning.isChecked())
//                    {
//                        isRunning = true;
//                        App.showLog(TAG + "==isRunning==" + isRunning);
//                    }
//                }
//            });

            //
            tvSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //
                    String tempTargetSteps = "";
                    String tempWeight = "";

                    if (etTargetSteps.getText().toString().length() > 0 &&
                            !etTargetSteps.getText().toString().equalsIgnoreCase(""))
                    {
                        tempTargetSteps = etTargetSteps.getText().toString();
                    } else {
                        tempTargetSteps = App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps);
                    }

                    App.showLog("==0000==tempTargetSteps==" + tempTargetSteps);


//                    if (etBodyWeight.getText().toString().length() > 0 &&
//                            !etBodyWeight.getText().toString().equalsIgnoreCase(""))
//                    {
//                        tempWeight = etBodyWeight.getText().toString();
//                    } else {
//                        tempWeight = App.sharePrefrences.getStringPref(PreferencesKeys.strWeight);
//                    }


                    //
                    if (strFrom.equalsIgnoreCase("ActSplashScreen")) {
                        App.sharePrefrences.setPref(PreferencesKeys.isSettingPageFirstTime, App.FALSE);
                        popupConfirmMessage(tempTargetSteps/*, tempWeight*/);
                    } else {
                        storeTargetStepsAndNevigate(tempTargetSteps/*, tempWeight*/);
                    }
                }
            });

            //
            tvSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String temp = etTargetSteps.getText().toString();
//                    String tempWeight = etBodyWeight.getText().toString();
                    if (strFrom.equalsIgnoreCase("ActSplashScreen")) {
                        App.sharePrefrences.setPref(PreferencesKeys.isSettingPageFirstTime, App.FALSE);
                        popupConfirmMessage(temp/*, tempWeight*/);
                    }
                }
            });
        } catch (Exception e) {e.printStackTrace();}
    }


    private void popupConfirmMessage(String target_steps/*, String weight*/) {
        try
        {
            final Dialog dialog = new Dialog(ActSettings.this);
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // for dialog shadow
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.popup_exit);

            TextView tvExitMessage = (TextView) dialog.findViewById(R.id.tvMessage);
            TextView tvCancel = (TextView) dialog.findViewById(R.id.tvCancel);
            tvCancel.setVisibility(View.GONE);
            TextView tvOK = (TextView) dialog.findViewById(R.id.tvOk);

            tvExitMessage.setTypeface(App.getTribuchet_MS());
            tvCancel.setTypeface(App.getTribuchet_MS());
            tvOK.setTypeface(App.getTribuchet_MS());

            App.showLog("==1111==target_steps==" + target_steps);

            String message = "";
            //
            if (target_steps != null && target_steps.length() > 0) {
                App.sharePrefrences.setPref(PreferencesKeys.strTargetSteps, target_steps);
                message = "Congratulations, you have set " + target_steps + " steps.\nIn case if you wish to change goal than you can update from setting page.";
            } else {
                App.sharePrefrences.setPref(PreferencesKeys.strTargetSteps, App.DEFAULT_TARGET_STEPS);
                message = "Let's start with " + App.DEFAULT_TARGET_STEPS + " steps.\nIn case if you wish to change goal than you can update from setting page.";
            }

//            //
//            if (weight != null && weight.length() > 0) {
//                App.sharePrefrences.setPref(PreferencesKeys.strWeight, weight);
//            } else {
//                App.sharePrefrences.setPref(PreferencesKeys.strWeight, App.DEFAULT_WEIGHT);
//            }

            //
            if (isRunning == true) {
                App.sharePrefrences.setPref(PreferencesKeys.isRunning, App.TRUE);
            } else {
                App.sharePrefrences.setPref(PreferencesKeys.isRunning, App.FALSE);
            }

            tvExitMessage.setText(message);
            tvOK.setText("Let's roll");

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

                    dialog.dismiss();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            Intent iv = new Intent(ActSettings.this, ActDashboard.class);
                            App.myStartActivity(ActSettings.this, iv);

                            /*
                            * Insert into DB - Target Steps
                            * */
                            TargetStepsModel model = new TargetStepsModel();
                            model.target_steps_date = App.getCurrentDate();
                            model.target_steps_count = App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps);
                            App.getDatabaseHelper().addTargetStepsCount(model);
                            ///////////////////////////
                        }
                    }, 180);

                }
            });
        } catch (Exception e) {e.printStackTrace();}
    }


    private void storeTargetStepsAndNevigate(String targetSteps/*, String weight*/) {
        try
        {
            /*
            * Insert into DB - Target Steps
            * */
            TargetStepsModel model = new TargetStepsModel();
            model.target_steps_date = App.getCurrentDate();
            model.target_steps_count = targetSteps;
            App.getDatabaseHelper().addTargetStepsCount(model);

            App.sharePrefrences.setPref(PreferencesKeys.strTargetSteps, targetSteps);
//            App.sharePrefrences.setPref(PreferencesKeys.strWeight, weight);
//            if (isRunning == true) {
//                App.sharePrefrences.setPref(PreferencesKeys.isRunning, App.TRUE);
//            } else {
//                App.sharePrefrences.setPref(PreferencesKeys.isRunning, App.FALSE);
//            }

            App.myFinishActivity(ActSettings.this);

        } catch (Exception e) {e.printStackTrace();}
    }


    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        App.myFinishActivity(ActSettings.this);
    }
}
