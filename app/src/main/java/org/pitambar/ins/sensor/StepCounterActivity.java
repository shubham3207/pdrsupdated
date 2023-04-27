package org.pitambar.ins.sensor;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.pitambar.ins.MySurfaceView;
import org.pitambar.ins.MyTextureView;
import org.pitambar.ins.R;
import org.w3c.dom.Text;

/**
 * Created by surensth on 25/12/2022.
 */


public class StepCounterActivity extends Activity {

    private TextView stepCountView;
    float[] velocity = new float[3];  // initial velocity
    float[] position = new float[3];  // initial position
    float[] acceleration = new float[3];  // current acceleration
    float[] lastVelocity = new float[3];  // previous velocity
    float[] lastPosition = new float[3];  // previous position
    int stepCount = 0;

    long lastTimestamp = 0;
    double dt = 0.2;  // time interval


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        stepCountView = findViewById(R.id.step_count);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);



        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    long timestamp = event.timestamp;
                    if (lastTimestamp != 0) {
                        dt = (timestamp - lastTimestamp) / 1.0e9;  // convert nanoseconds to seconds
                        float[] newAcceleration =  event.values;
                        velocity[0] += 0.5 * (acceleration[0] + newAcceleration[0]) * dt;
                        velocity[1] += 0.5 * (acceleration[1] + newAcceleration[1]) * dt;
                        velocity[2] += 0.5 * (acceleration[2] + newAcceleration[2]) * dt;
                        position[0] += 0.5 * (velocity[0] + lastVelocity[0]) * dt;
                        position[1] += 0.5 * (velocity[1] + lastVelocity[1]) * dt;
                        position[2] += 0.5 * (velocity[2] + lastVelocity[2]) * dt;
                        acceleration = newAcceleration;

                        // detect peak in the vertical position data
                        double delta_position = position[1] - lastPosition[1];
                        if (delta_position > 0 && lastPosition[1] < position[1] && position[1] > position[0]) {
                            // peak detected
                            stepCount++;
                            Log.d(TAG, "Step count: " + stepCount);
                        }

                        stepCountView.setText("Step Count: "+stepCount);

                        lastVelocity = velocity.clone();
                        lastPosition = position.clone();
                    }
                    lastTimestamp = timestamp;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        }, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);



    }
}
