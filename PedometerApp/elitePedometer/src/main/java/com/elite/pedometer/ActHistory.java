package com.elite.pedometer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elite.pedometer.util.DatabaseHelper;
import com.elite.pedometer.util.PreferencesKeys;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by dhaval.mehta on 22-Sep-2017.
 */

public class ActHistory extends BaseActivity {

    String TAG = "==ActHistory==";

    RelativeLayout rlNoData;
    TextView tvNoData;

    RelativeLayout rlMain;
    DatabaseHelper dbHelper;
    TextView tvDate, tvStepsCountAndLastTime;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<StepsDateModel> dbArrayDt = new ArrayList<>();

    ArrayList<StepsModel> arryHistory = new ArrayList<>();
    HistoryAdapter adapter;
    String strFrom = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try
        {
            super.onCreate(savedInstanceState);
            App.showLogTAG(TAG);
            ViewGroup.inflate(this, R.layout.act_history, llContainerSub);
            dbHelper = new DatabaseHelper(ActHistory.this);

            getIntentData();
            initialisation();
            setFonts();
            setClickEvents();
            setDataToRecyclerView();

        } catch (Exception e) {e.printStackTrace();}
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
            tvTitle.setText(App.TITLE_HISTORY);
            ivAppLogo.setImageResource(R.drawable.ic_back);

            //
            rlNoData = (RelativeLayout) findViewById(R.id.rlNoData);
            tvNoData = (TextView) findViewById(R.id.tvNoData);
            tvNoData.setTypeface(App.getTribuchet_MS());
            rlNoData.setVisibility(View.GONE);

            rlMain = (RelativeLayout) findViewById(R.id.rlMain);
            tvDate = (TextView) findViewById(R.id.tvDate);
            tvStepsCountAndLastTime = (TextView) findViewById(R.id.tvStepsCountAndLastTime);

            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);
//            RecyclerView.ItemDecoration itemDecoration =
//                    new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
//            recyclerView.addItemDecoration(itemDecoration);

        } catch (Exception e) {e.printStackTrace();}
    }


    private void setFonts() {
        try {
            //
            tvDate.setTypeface(App.getTribuchet_MS());
            tvStepsCountAndLastTime.setTypeface(App.getTribuchet_MS());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setClickEvents() {
        try {
            //
            ivAppLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

            //
            ivSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent iv = new Intent(ActHistory.this, ActSettings.class);
                    iv.putExtra(App.ITAG_FROM, "ActHistory");
                    App.myStartActivity(ActHistory.this, iv);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setDataToRecyclerView() {
        try
        {
            ArrayList<StepsDateModel> arrayDt = new ArrayList<>();
            ArrayList<StepsCountModel> array = null;
            arrayDt = (ArrayList<StepsDateModel>) dbHelper.getAllStepsDates();

            if (arrayDt.size() == 0)
            {
                App.showLogTAG(TAG + "==arrayDt DB size is null==Calling simple api==");
                rlNoData.setVisibility(View.VISIBLE);
                rlMain.setVisibility(View.GONE);
            }
            else
            {
                final ArrayList<StepsDateModel> finalArrayDt = arrayDt;

                rlNoData.setVisibility(View.GONE);
                rlMain.setVisibility(View.VISIBLE);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //stuff that updates ui

                        for (int i = 0; i < finalArrayDt.size() ; i++)
                        {
                            StepsDateModel stepsDtModel = new StepsDateModel();
                            stepsDtModel.steps_date = finalArrayDt.get(i).steps_date;
                            App.showLogTAG(TAG + "==fromDB==stepCount==DATE_ONLY==" + "\n"
                                    + i + " ==date==" + stepsDtModel.steps_date);
                            dbArrayDt.add(stepsDtModel); // i.e. 15-09-2017, 16-09-2017 etc

                            long dtWiseCount = App.getDatabaseHelper().getSUMCountStepsDateWise(stepsDtModel.steps_date);

                            App.showLog("==DateWise-totalCount==" + "\n"+
                                    "Date : " + stepsDtModel.steps_date +
                                    " //**// Count : " + dtWiseCount);

                            String strLastUpdateDateTime = App.getDatabaseHelper().getLastStepsDateTime(stepsDtModel.steps_date);
                            App.showLog(TAG + "==lastRecordOfDate==" + strLastUpdateDateTime);

                            String strTargetStepsForDt = App.getDatabaseHelper().getTargetStepsDateWise(stepsDtModel.steps_date);
                            App.showLog(TAG + "==strTargetStepsForDt==" + strTargetStepsForDt);


                            StepsModel model = new StepsModel();
                            model.steps_count = (int) dtWiseCount;
                            model.steps_date = stepsDtModel.steps_date;
                            model.steps_date_time = strLastUpdateDateTime;
                            model.target_steps_count = strTargetStepsForDt;
                            arryHistory.add(model);

                        } // for loop over here

                        App.showLog("==arryHistory.size()==" + arryHistory.size());

                        // Collections.sort(arryHistory, Collections.reverseOrder());
                        Collections.reverse(arryHistory);
                        adapter = new HistoryAdapter(arryHistory);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                    }
                });
            }


        } catch (Exception e) {e.printStackTrace();}
    }


    public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.DataObjectHolder> {

        private ArrayList<StepsModel> mDataset;

        public class DataObjectHolder extends RecyclerView.ViewHolder
        {
            // TextView tvDate, tvStepsCountAndLastTime;

            // New row item xml
            TextView tvDateDayMonth, tvDateYear, tvTargetSetps, tvTagTargetSetps, tvStpes, tvTagSteps,
                    tvCaloriesBurn, tvTagCaloriesBurn, tvDistance, tvTagDistance;

            public DataObjectHolder(View itemView)
            {
                super(itemView);
//                tvDate = (TextView) itemView.findViewById(R.id.tvDate);
//                tvStepsCountAndLastTime = (TextView) itemView.findViewById(R.id.tvStepsCountAndLastTime);

                tvDateDayMonth = (TextView) itemView.findViewById(R.id.tvDateDayMonth);
                tvDateYear = (TextView) itemView.findViewById(R.id.tvDateYear);

                tvTargetSetps = (TextView) itemView.findViewById(R.id.tvTargetSetps);
                tvTagTargetSetps = (TextView) itemView.findViewById(R.id.tvTagTargetSetps);

                tvStpes = (TextView) itemView.findViewById(R.id.tvStpes);
                tvTagSteps = (TextView) itemView.findViewById(R.id.tvTagSteps);

                tvCaloriesBurn = (TextView) itemView.findViewById(R.id.tvCaloriesBurn);
                tvTagCaloriesBurn = (TextView) itemView.findViewById(R.id.tvTagCaloriesBurn);

                tvDistance = (TextView) itemView.findViewById(R.id.tvDistance);
                tvTagDistance = (TextView) itemView.findViewById(R.id.tvTagDistance);
            }
        }

        public HistoryAdapter(ArrayList<StepsModel> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_history_item_new, parent, false);

            DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
            return dataObjectHolder;
        }

        @Override
        public void onBindViewHolder(DataObjectHolder holder, int position) {

            try
            {
                holder.tvDateDayMonth.setTypeface(App.getTribuchet_MS());
                holder.tvDateYear.setTypeface(App.getTribuchet_MS());

                holder.tvTargetSetps.setTypeface(App.getTribuchet_MS());
                holder.tvTagTargetSetps.setTypeface(App.getTribuchet_MS());

                holder.tvStpes.setTypeface(App.getTribuchet_MS());
                holder.tvTagSteps.setTypeface(App.getTribuchet_MS());

                holder.tvCaloriesBurn.setTypeface(App.getTribuchet_MS());
                holder.tvTagCaloriesBurn.setTypeface(App.getTribuchet_MS());

                holder.tvDistance.setTypeface(App.getTribuchet_MS());
                holder.tvTagDistance.setTypeface(App.getTribuchet_MS());

                //
                String strDate = mDataset.get(position).steps_date;
                String strTargetSteps = mDataset.get(position).target_steps_count;
                String strTodaysSteps = "" + mDataset.get(position).steps_count;

                //
                if (strDate != null && strDate.length() > 0)
                {
                    holder.tvDateDayMonth.setText(App.getdd_RD_TH_MMM(strDate));
                    holder.tvDateYear.setText(App.getYYYY(strDate));
                }

                //
                if (strTargetSteps != null && strTargetSteps.length() > 0)
                {
                    Spanned text = Html.fromHtml("<b>"+ strTargetSteps + "</b>");
                    holder.tvTargetSetps.setText(text);
                }

                //
                if (strTodaysSteps != null && strTodaysSteps.length() > 0)
                {
                    Spanned text = Html.fromHtml("<b>"+ strTodaysSteps + "</b>");
                    holder.tvStpes.setText(text);


                    if (Integer.parseInt(strTodaysSteps) != 0)
                    {
                        double calory = 0;
                        double distance = 0;
                        for (int k = 0 ; k< Integer.parseInt(strTodaysSteps); k++)
                        {
//                            calory +=
//                                    (Integer.parseInt(App.sharePrefrences.getStringPref(PreferencesKeys.strWeight))
//                                            * App.METRIC_RUNNING_FACTOR)// Distance:
//                                            * Integer.parseInt(App.DEFAULT_STEP_LENGTH) // centimeters
//                                            / 100000.0; // centimeters/kilometer


                            distance += (float)(// kilometers
                                    Integer.parseInt(App.DEFAULT_STEP_LENGTH) // centimeters
                                            / 100000.0); // centimeters/kilometer

                        }

                        calory = Integer.parseInt(strTodaysSteps) / App.DEFAULT_STEPS_BY_CALORY;
                        Spanned textCalory = Html.fromHtml("<b>"+ (int) calory + "</b>");
                        holder.tvCaloriesBurn.setText(textCalory);

                        Spanned textDistance = Html.fromHtml("<b>"+ String.format("%.3f", distance) + "</b> km");
                        holder.tvDistance.setText(textDistance);

                        App.showLog(TAG + "==getBurnCalories==" + calory);
                        App.showLog(TAG + "==getDistance==" + distance);
                    }
                    else
                    {
                        Spanned textCalory = Html.fromHtml("<b>"+ 0 + "</b>");
                        holder.tvCaloriesBurn.setText(textCalory);

                        Spanned textDistance = Html.fromHtml("<b>"+ 0.000 + "</b> km");
                        holder.tvDistance.setText(textDistance);
                    }

                }

            } catch (Exception e) {e.printStackTrace();}


            // OLD
//            holder.tvDate.setTypeface(App.getTribuchet_MS());
//            holder.tvStepsCountAndLastTime.setTypeface(App.getTribuchet_MS());

//            holder.tvDate.setText(App.getddMMMyy(mDataset.get(position).steps_date));
//
////            Spanned text = Html.fromHtml("<b>"+ mDataset.get(position).steps_count + "</b> , till <i>" + mDataset.get(position).steps_date_time + "</i>");
//            Spanned text = Html.fromHtml("<b>"+ mDataset.get(position).steps_count + "</b> - <b>" + mDataset.get(position).target_steps_count + "</b>");
//            holder.tvStepsCountAndLastTime.setText(text);
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }


    @Override
    public void onBackPressed() {

        App.myFinishActivity(ActHistory.this);

//        if (strFrom.equalsIgnoreCase("ActDashboard"))
//        {
//            Intent iv = new Intent(ActHistory.this, ActDashboard.class);
//            startActivity(iv);
//            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//        }
//        else if (strFrom.equalsIgnoreCase("ActSettings"))
//        {
//            Intent iv = new Intent(ActHistory.this, ActSettings.class);
//            startActivity(iv);
//            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//        }
    }
}
