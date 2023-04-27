

package org.sakshi.sakshipdr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends Activity implements OnClickListener, SensorEventListener {

    private final float N2S = 1000000000f;

    //Initial Camera Pos/Att
    private float[] INIPos = {500, 500, 0};
    private float[] INIVel = {0, 0, 0};
    private float[] INICbn = {1, 0, 0, 0, 1, 0, 0, 0, 1};

    private GLSurfaceView mGLSView;
    private SensorManager mSMan;
    private MyCube mCube;
    private INS mINS;
    private Kalman mKalman;

    //Debug Text View
    TextView etv;

    //Mode of the program
    //0 = 3Dof (Acc+Compass)
    //1 = 3Dof (Gyro)
    //2 = 6Dof
    //3 = Bias Removal
    int mode = -1;
    int count = 0;
    boolean ZFlag = false, CFlag = false;

    //Most recent sensor data and its timestamp
    float[] dAcc = new float[3];
    float[] dGyro = new float[3];
    float[] dMag = new float[3];
    long etime = 0; //Time for the latest sensor data
    long ptAcc = 0, ptGyro = 0, ptMag = 0;    //previous Sample Time

    //Sensor Calibration values
    float[] cBAcc = new float[3];
    float[] cBGyro = new float[3];

    //Periods
    private final long PERBR = (long) (1 * N2S);    //Data collection period for bias removal (Nanosec)
    private long tBR = 0;
    private final long PERPROP = (long) (1 * N2S);    //KF maximum covariance propagation period (Nanosec)
    private long ptProp = 0, tProp = 0;

    //Temporary variables to be used (used to redeuce the load of GC)
    float[] vr_a = new float[3];
    float[] vr_b = new float[3];
    float[] mx_a = new float[9];

    private double[] lastPosition = new double[3];
    private int stepCount = 0;

    TextView distanceTextView;
    TextView stepTextView;

    MyTextureView mCustomTexttureView;

    float xScaleFactor = 0f;
    float yScaleFactor = 0f;

    private ArrayList<float[]> positions = new ArrayList<>();
    private ArrayList<float[]> positionsForDistance = new ArrayList<>();
    private ArrayList<float[]> velocities = new ArrayList<>();

    private static final float STEP_THRESHOLD = 0.5f;
    private static final int STEP_DELAY_MS = 250;

    private Queue<Float> window = new LinkedList<>();
    private long lastStepTime = 0;
    private double[] lastVelocity = new double[3];

    private double[] distanceTravelled = new double[3];
    private float[] previousPosition = new float[3];
    private float[] previousVelocity = new float[3];
    float totalDistanceGlobal = 0.0f;
    long previousTimeForDisplacement = 0;


    public MainActivity() {
        mCube = new MyCube(INIPos, INICbn);
        mKalman = new Kalman();

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mSMan = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        Bitmap mImage = BitmapFactory.decodeResource(getResources(), R.drawable.white_map);
        float mImageWidth = mImage.getWidth();
        float mImageHeight = mImage.getHeight();

        xScaleFactor = mImageWidth / 975;
        yScaleFactor = mImageHeight / 1250;
        INIPos[0] = mImageWidth / 2;
        INIPos[1] = mImageHeight / 2;
        INIPos[2] = 0f;

        mINS = new INS(INIPos, INIVel, INICbn);

        //Set the OpenGl View
        mGLSView = new GLSurfaceView(this);
        mGLSView.setRenderer(mCube);
        setContentView(mGLSView);
        mGLSView.setVisibility(View.INVISIBLE);

        RelativeLayout mSurface = new RelativeLayout(this);
        mSurface.setGravity(Gravity.CENTER);
        mSurface.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mSurface.setId(92);

        mCustomTexttureView = new MyTextureView(this, width, height, mImage);
        mSurface.addView(mCustomTexttureView);

        LinearLayout llTop = new LinearLayout(this);
        llTop.setOrientation(LinearLayout.VERTICAL);
        llTop.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

//        //Add button and text elements to the view
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
//
        LinearLayout llb1 = new LinearLayout(this);
        llb1.setOrientation(LinearLayout.HORIZONTAL);
        llb1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        llb1.setGravity(Gravity.CENTER);
//
        LinearLayout llb2 = new LinearLayout(this);
        llb2.setOrientation(LinearLayout.HORIZONTAL);
        llb2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        llb2.setGravity(Gravity.CENTER);

        LinearLayout llb3 = new LinearLayout(this);
        llb3.setOrientation(LinearLayout.VERTICAL);
        llb3.setBackgroundColor(Color.WHITE);
        llb3.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        llb3.setGravity(Gravity.CENTER);
//
//        Button mbut0 = new Button(this);
//        mbut0.setText("3Dof(A+C)");
//        mbut0.setId(0);
//        Button mbut1 = new Button(this);
//        mbut1.setText("3DoF(Gyro)");
//        mbut1.setId(1);
        Button mbut3 = new Button(this);
        mbut3.setText("Start");
        mbut3.setId(3);
//
        Button mbut2 = new Button(this);
        mbut2.setText("Bias Rem.");
        mbut2.setId(2);
        Button mbut4 = new Button(this);
        mbut4.setText("UPDATE");
        mbut4.setId(4);
        Button mbut5 = new Button(this);
        mbut5.setText("CURRENT");
        mbut5.setId(5);
        Button mbut6 = new Button(this);
        mbut6.setText("Reset");
        mbut6.setId(6);
        mbut6.setVisibility(View.GONE);
        llb1.addView(mbut3);
        llb2.addView(mbut2);
        llb2.addView(mbut4);
        llb2.addView(mbut5);
        llb2.addView(mbut6);
//
        etv = new TextView(this);
        etv.setPadding(28, 14, 28, 14);
        etv.setText("Initial");
        distanceTextView = new TextView(this);
        distanceTextView.setId(600);
        stepTextView = new TextView(this);
        stepTextView.setId(601);
        distanceTextView.setPadding(10, 5, 10, 5);
        stepTextView.setPadding(10, 5, 10, 5);
        distanceTextView.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
        stepTextView.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);

        llb3.addView(stepTextView);
        llb3.addView(distanceTextView);
        llb3.addView(etv);
        ll.addView(llb1);
        ll.addView(llb2);
        ll.addView(llb3);


//        LinearLayout llTopLayout = new LinearLayout(this);
//        llTopLayout.setOrientation(LinearLayout.VERTICAL);
//        llTopLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
//        llTopLayout.addView(distanceTextView);

        this.addContentView(mSurface, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        this.addContentView(ll, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        mbut2.setOnClickListener(this);
        mbut3.setOnClickListener(this);
        mbut4.setOnClickListener(this);
        mbut5.setOnClickListener(this);
        mbut6.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //OpenGl View
        mGLSView.onResume();

        //Sensors
        Sensor sAcc = mSMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sMag = mSMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor sGyro = mSMan.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        int rate = SensorManager.SENSOR_DELAY_FASTEST;

        mSMan.registerListener(this, sAcc, rate);
        if (sMag != null) mSMan.registerListener(this, sMag, rate);
        if (sGyro != null)
            mSMan.registerListener(this, sGyro, rate);
        else
            etv.setText("Not Started Yet");
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSView.onPause();
        mSMan.unregisterListener(this);
    }

    @Override
    public void onClick(View v) {
        int cmd = v.getId();
        flow_control(cmd);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do nothing
    }

    public void onSensorChanged(SensorEvent event) {
        int etype = event.sensor.getType();
        etime = event.timestamp;
        float dt = 0;
        //Recod the value and time
        switch (etype) {
            case Sensor.TYPE_ACCELEROMETER:
                dAcc[0] = event.values[0] - cBAcc[0];
                dAcc[1] = event.values[1] - cBAcc[1];
                dAcc[2] = event.values[2] - cBAcc[2];
                if (ptAcc != 0) dt = (etime - ptAcc) / N2S;
                ptAcc = etime;
                break;
            case Sensor.TYPE_GYROSCOPE:
                dGyro[0] = event.values[0] - cBGyro[0];
                dGyro[1] = event.values[1] - cBGyro[1];
                dGyro[2] = event.values[2] - cBGyro[2];
                if (ptGyro != 0) dt = (etime - ptGyro) / N2S;
                ptGyro = etime;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                dMag[0] = event.values[0];
                dMag[1] = event.values[1];
                dMag[2] = event.values[2];
                if (ptMag != 0) dt = (etime - ptMag) / N2S;
                ptMag = etime;

                break;
        }

        if (mode == 0) { //3dof based on Acc+Compass
            //We have to be sure that neither Acc nor Compass is zero.
            if ((dAcc[0] * dAcc[0] + dAcc[1] * dAcc[1] + dAcc[2] * dAcc[2]) < 80)
                return;
            else if ((dMag[0] * dMag[0] + dMag[1] * dMag[1] + dMag[2] * dMag[2]) < 400) //30ut*30ut
                return;

            //I am going to use android's built function for body vector to dcm transformation (see the development notes for the criticism of this method)
            SensorManager.getRotationMatrix(mx_a, null, dAcc, dMag);
            //Set the new orientation in INS and Cube classes
            mCube.set_dcm(mx_a);
            mINS.set_dcm(mx_a);
        } else if (mode == 1) { //3Dof Gyro (mINS must have an initial orientation)
            if (etype == Sensor.TYPE_GYROSCOPE && dt > 0) {
                //Update attitude
                mINS.update_attI(dGyro, dt);

                //Set the camera orientation for cube
                mCube.set_dcm(mINS.get_dcm());
            }

        } else if (mode == 2) { //Bias removal under LEVEL and STATIONARY conditions
            if (etype == Sensor.TYPE_ACCELEROMETER)
                mINS.accum.addacc(dAcc);

            if (etype == Sensor.TYPE_GYROSCOPE)
                mINS.accum.addgyro(dGyro);

            if (etime > tBR) {
                int i;
                //Set the bias values
                mINS.get_gravity(vr_a);
                mINS.accum.avacc(cBAcc);
                for (i = 0; i < 3; i++)
                    cBAcc[i] += vr_a[i];    //Gravity is in ned. That is why we add them.

                mINS.accum.avgyro(cBGyro);

                //Reset the accumulators and time
                mINS.accum.clear();
                tBR = 0;
                //Set the mode to 0 automatically after this routine
                flow_control(0);
                etv.setText("Abias :" + cBAcc[0] + "\n" + cBAcc[1] + "\n" + cBAcc[2] + "\n" +
                        "Gbias :" + cBGyro[0] + "\n" + cBGyro[1] + "\n" + cBGyro[2]);
            }

        } else if (mode == 3) {    //6Dof Calculations
            if (etype == Sensor.TYPE_ACCELEROMETER) { //Update velocity and Pos
                //Update pos and vel
                mINS.update_velI(dAcc, dt);
                mINS.update_posII(dt);
                //Update acc accum
                mINS.accum.addacc(dAcc);
            }

            if (etype == Sensor.TYPE_GYROSCOPE) { //Update velocity and Pos
                //Update pos and vel
                mINS.update_attI(dGyro, dt);
                mINS.update_velII(dGyro, dt);
                mINS.update_posI(dGyro, dt);

                //Update acc accum
                mINS.accum.addgyro(dGyro);
            }

            //Set the camera pos & orientation for cube
            mCube.set_dcm(mINS.get_dcm());
            mCube.set_pos(mINS.get_pos());

            //State Updates and Covariance propagation
            if (etime > tProp || ZFlag || CFlag) {
                //First update (propagate the covariance to the current time)
                dt = (etime - ptProp) / N2S;
                ptProp = etime;

                //Propagate the covariance
                mKalman.Propagate(mINS, dt);

                //Clear sensor data accumulators
                mINS.accum.clear();

                //Next Covaraince update time
                tProp = etime + PERPROP;

                //Debug screen
                etv.setText("Pos :" + mINS.Pos_b.data[0] + ", " + mINS.Pos_b.data[1] + ", " + mINS.Pos_b.data[2] + "\n" +
                        "Vel :" + mINS.Vel_b.data[0] + ", " + mINS.Vel_b.data[1] + ", " + mINS.Vel_b.data[2]);

                float[] devicePosition = new float[3];

                devicePosition[0] = (float) mINS.Pos_b.data[0];
                devicePosition[1] = (float) mINS.Pos_b.data[1];
                devicePosition[2] = (float) mINS.Pos_b.data[2];

                float[] velocity = new float[3];
                velocity[0] = (float) mINS.Vel_b.data[0];
                velocity[1] = (float) mINS.Vel_b.data[1];
                velocity[2] = (float) mINS.Vel_b.data[2];

                velocities.add(velocity);

                if (isStep(dAcc, dGyro, dMag, etime)) {
                    stepCount++;
                    positionsForDistance.add(devicePosition);
                    velocities.add(velocity);
                    float distance = 0.0f;
                    float interval = 0.0f;
                    if (previousTimeForDisplacement == 0)
                        interval = 0;
                    else
                        interval = (etime - previousTimeForDisplacement) / N2S;
                    distance = calculateDistance(positionsForDistance, velocities, interval);
                    previousTimeForDisplacement = etime;
                    totalDistanceGlobal = totalDistanceGlobal + distance;

                }

                final DecimalFormat df = new DecimalFormat("0.00");
                distanceTextView.setText("Distance Travelled: " + df.format(totalDistanceGlobal * 0.01) + " m");
                stepTextView.setText("Step Count: " + stepCount);
//                stepTextView.setText("Mag: "+magnitude+", verifier: "+ magnitudeVerifier);

                Log.d("Step", "" + stepCount);

                positions.add(devicePosition);

                if (positions.size() > 3) {
                    float changeInX = devicePosition[0] - positions.get(positions.size() - 2)[0];
                    float changeInY = devicePosition[1] - positions.get(positions.size() - 2)[1];
                    mCustomTexttureView.setmVelocityX(changeInX);
                    mCustomTexttureView.setmVelocityY(changeInY);
                } else {
                    mCustomTexttureView.setmVelocityX(0f);
                    mCustomTexttureView.setmVelocityY(0f);
                }
                mCustomTexttureView.setmPositionArray(positions);
                mCustomTexttureView.drawImage();
//                }

                flow_control(4);

                //Check if there is an update request
                if (ZFlag) { //Process Zupt request
                    mKalman.applyZupt(mINS, cBAcc, cBGyro);
                    ZFlag = false;
                }
                if (CFlag) { //Process Cupt Request
                    mKalman.applyCupt(mINS, cBAcc, cBGyro, INIPos);
                    CFlag = false;
                }

            }

        }
    }

    private float calculateDistance(ArrayList<float[]> positions, ArrayList<float[]>
            velocityData, float timeInterval) {
        float[] distanceTravelled = new float[3];

        float[] displacement = new float[3];
        for (int j = 0; j < 3; j++) {
            displacement[j] = (float) ((positions.get(positions.size() - 1)[j] - previousPosition[j]) / 2 * timeInterval +
                    (velocityData.get(velocityData.size() - 1)[j] - previousVelocity[j]) / 2 * timeInterval);
            distanceTravelled[j] = displacement[j];
        }

        previousPosition = positions.get(positions.size() - 1);
        previousVelocity = velocityData.get(velocityData.size() - 1);


// The total distance travelled is the magnitude of the distanceTravelled vector
        float totalDistance = (float) Math.sqrt(distanceTravelled[0] * distanceTravelled[0] +
                distanceTravelled[1] * distanceTravelled[1] +
                distanceTravelled[2] * distanceTravelled[2]);

        return totalDistance;
    }


    //Decides what to do
    private void flow_control(int cmd) {
        if (cmd == 0) {
            mode = 0;
            //Reset the pos and velocity (attitude will be set at each update)
            mINS.set_pos(INIPos);
            mINS.set_vel(INIVel);
            mCube.set_pos(mINS.get_pos());
        } else if (cmd == 1) {
            mode = 1;
            //Reset the pos and velocity (gyro data updates the previous attitude)
            mINS.set_pos(INIPos);
            mINS.set_vel(INIVel);

            mCube.set_pos(mINS.get_pos());

            //Debug
            etv.setText("Switched to Gyro mode");
        } else if (cmd == 2) {    //Bias Removal
            if (etime > 0) {
                mode = 2;
                //Reset the previous bias estimates and sensor data accumulators
                for (int i = 0; i < 3; i++) {
                    cBAcc[i] = 0;
                    cBGyro[i] = 0;
                }
                mINS.accum.clear();

                //Maximum time to stay in this mode
                tBR = etime + PERBR;
            }
        } else if (cmd == 3) {    //6Dof
            mode = 3;

            //Covariance propagation times
            ptProp = etime;    //Implicitly assumes that there were at least one sensor sample before coming to this point
            tProp = ptProp + PERPROP;

            //Clear accumulators
            mINS.accum.clear();

            //Clear Update Flags
            ZFlag = false;
            CFlag = false;

            //Reset the covariance
            mKalman.initP();
        } else if (cmd == 4) {    //Zupt
            if (mode == 3)
                ZFlag = true;
            else
                etv.setText("INS update not Started yet.");
        } else if (cmd == 5) {    //Cupt for the initial Position
            if (mode == 3)
                CFlag = true;
            else
                etv.setText("INS update not Started yet.");
        } else if (cmd == 6) {    //Reset the states
            //Reset the INS
            mINS.set_pos(INIPos);
            mINS.set_vel(INIVel);
            mINS.set_dcm(INICbn);
            for (int i = 0; i < 3; i++) {
                cBAcc[i] = 0;
                cBGyro[i] = 0;
            }
            mINS.accum.clear();

            //Set the camera pos & orientation for cube
            mCube.set_dcm(mINS.get_dcm());
            mCube.set_pos(mINS.get_pos());

            //Reset Kalman
            ZFlag = false;
            CFlag = false;
            mKalman.initP();

            positions.clear();

            //change mode to 0
            mode = 0;
        }
    }

//    private class DevicePositionView extends View {
//        private float[] iDeviceOrientation = {0f, 0f, 0f};
//        private float[] iDevicePosition = {0f, 0f, 0f};
//        private Paint paint;
//        Context mContext;
//        private Canvas mCanvas;
//
//        private float[] mPositions;
//        private ArrayList<float[]> mPositionArray = new ArrayList<>();
//
////        public void setPosition(float[] positions) {
////            mPositions = positions;
////            invalidate();
////        }
//
//        public void setPositionArray(ArrayList<float[]> positionArray) {
//            mPositionArray = positionArray;
//        }
//
//        public DevicePositionView(Context context) {
//            super(context);
//            mContext = context;
//            paint = new Paint();
//            paint.setColor(getResources().getColor(R.color.colorPrimary));
//            paint.setStyle(Paint.Style.FILL);
//        }
//
//        @Override
//        protected void onDraw(Canvas canvas) {
//            super.onDraw(canvas);
//            mCanvas = canvas;
//            // Set up the paint for the circle
//            Paint paint = new Paint();
//            paint.setColor(Color.RED);
//            paint.setStyle(Paint.Style.FILL);
//            canvas.save();
//            // Draw the circle at the current position
//            if (mPositionArray.size() > 0) {
//                for (int i = 0; i < mPositionArray.size(); i++) {
//                    canvas.drawCircle(mPositionArray.get(i)[0], mPositionArray.get(i)[1], 10f, paint);
//                    if (mPositionArray.size() > 1 && i <= mPositionArray.size() - 2) {
//                        canvas.drawLine(mPositionArray.get(i)[0], mPositionArray.get(i)[1], mPositionArray.get(i + 1)[0], mPositionArray.get(i + 1)[1], paint);
//                    }
//                }
//                postInvalidate();
//            }
//            canvas.restore();
//        }
//    }

    public boolean isStep(float[] accelerometer, float[] magnetometer, float[] gyroscope, long timestamp) {
        // Calculate magnitude of acceleration vector
        float accelerationMagnitude = (float) Math.sqrt(accelerometer[0] * accelerometer[0] + accelerometer[1] * accelerometer[1] + accelerometer[2] * accelerometer[2]);

        // Calculate the magnitude of the magnetic field vector
        float magneticMagnitude = (float) Math.sqrt(magnetometer[0] * magnetometer[0] + magnetometer[1] * magnetometer[1] + magnetometer[2] * magnetometer[2]);

        // Calculate the angular velocity vector
        float angularVelocity = (float) Math.sqrt(gyroscope[0] * gyroscope[0] + gyroscope[1] * gyroscope[1] + gyroscope[2] * gyroscope[2]);

        // Combine the data from the three sensors to calculate the step detection value
        float stepDetectionValue = accelerationMagnitude + magneticMagnitude + angularVelocity;

        // Add magnitude to the sliding window
        window.add(stepDetectionValue);

        // Remove oldest value if the window is larger than 25 samples
        if (window.size() > 25) {
            window.remove();
        }

        // Compute the mean of the window
        float mean = 0.0f;
        for (float value : window) {
            mean += value;
        }
        mean /= window.size();

        // Compute the standard deviation of the window
        float variance = 0.0f;
        for (float value : window) {
            variance += (value - mean) * (value - mean);
        }
        float standardDeviation = (float) Math.sqrt(variance / window.size());

        // Check for a peak
        boolean stepDetected = false;

        float magnitudeVerifier = mean + STEP_THRESHOLD * standardDeviation;
        if (stepDetectionValue > magnitudeVerifier && timestamp > lastStepTime + STEP_DELAY_MS) {
            stepDetected = true;
            lastStepTime = timestamp;
        }

        return stepDetected;
    }
}

