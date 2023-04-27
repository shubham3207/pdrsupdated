package org.sakshi.sakshipdr.sensor;

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
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.sakshi.sakshipdr.MySurfaceView;
import org.sakshi.sakshipdr.MyTextureView;
import org.sakshi.sakshipdr.R;



public class InsActivity extends Activity implements SensorEventListener {
    TextView positionTextView, orientationTextView;
    float[] deviceOrientation, devicePosition;

    RelativeLayout mChildLayout;

    MyTextureView mMyTextureView;
    MySurfaceView mMySurfaceView;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;

    private float[] accelerationValues = new float[3];
    private float[] gyroscopeValues = new float[3];
    private float[] magnetometerValues = new float[3];
    private float[] mOrientation = new float[3];
    private long mTimestamp = 0;
    private float[] mRotationMatrix = new float[9];

    private static final float ALPHA = 0.5f;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float EPSILON = 1e-6f;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ins);

        positionTextView = findViewById(R.id.posTextView);
        orientationTextView = findViewById(R.id.orientationTextView);

        mChildLayout = findViewById(R.id.childLayout);

        DisplayMetrics displayMetrics = new DisplayMetrics();

        // on below line we are getting metrics for display using window manager.
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        // on below line we are getting height
        // and width using display metrics.
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        mMyTextureView = new MyTextureView(this, width, height);
        mMySurfaceView = new MySurfaceView(this, width, height);

        mChildLayout.addView(mMyTextureView);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == magnetometer) {
            magnetometerValues[0] = ALPHA * magnetometerValues[0] + (1 - ALPHA) * event.values[0];
            magnetometerValues[1] = ALPHA * magnetometerValues[1] + (1 - ALPHA) * event.values[1];
            magnetometerValues[2] = ALPHA * magnetometerValues[2] + (1 - ALPHA) * event.values[2];
        } else if (event.sensor == gyroscope) {
            float[] deltaRotationVector = new float[4];
            if (mTimestamp != 0) {
                final float dT = (event.timestamp - mTimestamp) * NS2S;
                System.arraycopy(event.values, 0, gyroscopeValues, 0, 3);
                deltaRotationVector = getRotationVectorFromGyro(gyroscopeValues, deltaRotationVector, dT / 2.0f, dT);
            }
            mTimestamp = event.timestamp;
            float[] deltaRotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);

//            mRotationMatrix = matrixMultiplication(mRotationMatrix, deltaRotationMatrix);

            mOrientation = SensorManager.getOrientation(deltaRotationMatrix, mOrientation);

        } else if (event.sensor == accelerometer) {
            accelerationValues[0] = ALPHA * accelerationValues[0] + (1 - ALPHA) * event.values[0];
            accelerationValues[1] = ALPHA * accelerationValues[1] + (1 - ALPHA) * event.values[1];
            accelerationValues[2] = ALPHA * accelerationValues[2] + (1 - ALPHA) * event.values[2];
        }

    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int index = 3 * i + j;
                result[index] = 0;
                for (int k = 0; k < 3; k++) {
                    result[index] += A[3 * i + k] * B[3 * k + j];
                }
            }
        }
        return result;
    }


    private float[] getRotationVectorFromGyro(float[] gyroscopeValues, float[] deltaRotationVector, float v, float timeFactor) {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude = (float) Math.sqrt(gyroscopeValues[0] * gyroscopeValues[0] + gyroscopeValues[1] * gyroscopeValues[1] + gyroscopeValues[2] * gyroscopeValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if (omegaMagnitude > EPSILON) {
            normValues[0] = gyroscopeValues[0] / omegaMagnitude;
            normValues[1] = gyroscopeValues[1] / omegaMagnitude;
            normValues[2] = gyroscopeValues[2] / omegaMagnitude;
        }

        // Integrate the angular velocity over time to get a delta rotation from gyro data
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;

        return deltaRotationVector;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private class DevicePositionView extends View {

        private float[] iDeviceOrientation = {0f, 0f, 0f};
        private float[] iDevicePosition = {0f, 0f, 0f};
        private Paint paint;
        Canvas mCanvas;

        Context mContext;

        public DevicePositionView(Context context) {
            super(context);
            mContext = context;
            paint = new Paint();
            paint.setColor(getResources().getColor(R.color.colorPrimary));
            paint.setStyle(Paint.Style.FILL);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            mCanvas = canvas;
            iDeviceOrientation = deviceOrientation;
            iDevicePosition = devicePosition;

            float x = iDevicePosition[1];
            float y = iDevicePosition[2];

            mCanvas.save();
            mCanvas.rotate(iDeviceOrientation[2], x, y);
            mCanvas.drawCircle(x, y, 5f, paint);
            mCanvas.restore();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
