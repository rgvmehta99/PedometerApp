package com.elite.pedometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Created by dhaval.mehta on 22-Sep-2017.
 */

public class StepTrackerDetector implements SensorEventListener {

    String TAG = "==StepTrackerDetector==";

    private OnShakeListener mListener;
    private int mShakeCount;
    float last_x, last_y, last_z;
    long lastUpdate = 0;
    private static int SHAKE_THRESHOLD = 500;

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    public interface OnShakeListener {
        public void onShake(int count);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try
        {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER)
            {
                mShakeCount = (int) event.values[0];
                mListener.onShake(mShakeCount);
            }
            else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR)
            {
                if (event.values[0] == 1)
                {
                    mShakeCount++;
                    mListener.onShake(mShakeCount);
                }
            }
            else
            {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                long curTime = System.currentTimeMillis();
                // only allow one update every 100ms.
                if ((curTime - lastUpdate) > 100)
                {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;
                    float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                    if (speed > SHAKE_THRESHOLD)
                    {
                        mShakeCount++;
                        mListener.onShake(mShakeCount);
                    }

                    last_x = x;
                    last_y = y;
                    last_z = z;
                }
            }


        } catch (Exception e) {e.printStackTrace();}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
