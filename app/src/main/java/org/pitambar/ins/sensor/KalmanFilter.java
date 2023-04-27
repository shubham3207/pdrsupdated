package org.pitambar.ins.sensor;

import android.graphics.Matrix;
import android.util.Log;

/**
 * Created by surensth on 25/12/2022.
 */
public class KalmanFilter {
    // Declare variables to store the state vector and covariance matrix
    private float[] state;
    private float[][] covariance;

    // Declare variables to store the measurement matrix and noise covariance matrix
    private float[][] measurementMatrix;
    private float[][] measurementNoiseCovariance;

    // Declare variables to store the process model matrix and noise covariance matrix
    private float[][] processModelMatrix;
    private float[][] processNoiseCovariance;

    // Declare variables to store the control input vector and control input matrix
    private float[] controlInputVector;
    private float[][] controlInputMatrix;

    // Constructor
    public KalmanFilter() {
        // Initialize the state vector and covariance matrix
        state = new float[6];
        covariance = new float[6][6];
    }

    // Method to set the state vector and covariance matrix
    public void setState(float[] state, float[][] covariance) {
        this.state = state;
        this.covariance = covariance;
    }

    // Method to set the measurement matrix
// Method to set the measurement matrix and noise covariance matrix
    public void setMeasurement(float[][] measurementMatrix, float[][] measurementNoiseCovariance) {
        this.measurementMatrix = measurementMatrix;
        this.measurementNoiseCovariance = measurementNoiseCovariance;
    }

    // Method to set the process model matrix and noise covariance matrix
    public void setProcessModel(float[][] processModelMatrix, float[][] processNoiseCovariance) {
        this.processModelMatrix = processModelMatrix;
        this.processNoiseCovariance = processNoiseCovariance;
    }

    // Method to set the control input vector and control input matrix
    public void setControlInput(float[] controlInputVector, float[][] controlInputMatrix) {
        this.controlInputVector = controlInputVector;
        this.controlInputMatrix = controlInputMatrix;
    }

    // Method to update the Kalman filter with a new measurement
    public void update(float[] measurementVector) {
        Log.v("test", "test measurementVector obtained " + measurementVector[0]);

        // Predict the state and covariance
        state = MatrixMath.add(MatrixMath.multiply(processModelMatrix, state), controlInputVector);
        Log.v("test", "test state111 obtained " + controlInputVector[0]);

        covariance = MatrixMath.add(MatrixMath.multiply(MatrixMath.multiply(processModelMatrix, covariance), MatrixMath.transpose(processModelMatrix)), processNoiseCovariance);

        // Calculate the Kalman gain
        float[][] kalmanGain = MatrixMath.multiply(covariance, MatrixMath.inverse(MatrixMath.add(MatrixMath.multiply(measurementMatrix, covariance), measurementNoiseCovariance)));

        // Update the state and covariance
        state = MatrixMath.add(state, MatrixMath.multiply(kalmanGain, MatrixMath.subtract(measurementVector, MatrixMath.multiply(measurementMatrix, state))));
        covariance = MatrixMath.subtract(covariance, MatrixMath.multiply(kalmanGain, MatrixMath.multiply(measurementMatrix, covariance)));
        Log.v("test", "test state obtained " + state[0] + " covariance " + covariance[0]);

    }

    // Method to get the smoothed sensor data from the Kalman filter
    public float[] getState() {
        return state;
    }

    // Method to subtract two matrices
    public static float[][] subtract(float[][] matrix1, float[][] matrix2) {
        // Check that the matrices have the same size
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new IllegalArgumentException("Matrices must have the same size");
        }

        // Declare a new matrix to store the result
        float[][] result = new float[matrix1.length][matrix1[0].length];

        // Subtract the matrices element-wise
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                result[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }

        // Return the result
        return result;
    }


}
