package org.sakshi.sakshipdr.sensor;


public class MatrixMath {
    // Method to add two matrices
    public static float[][] add(float[][] matrix1, float[][] matrix2) {
        // Check that the matrices have the same size
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new IllegalArgumentException("Matrices must have the same size");
        }

        // Declare a new matrix to store the result
        float[][] result = new float[matrix1.length][matrix1[0].length];

        // Add the matrices element-wise
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                result[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }

        // Return the result
        return result;
    }
    // Method to add two vectors
    public static float[] add(float[] vector1, float[] vector2) {
        // Check that the vectors have the same size
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must have the same size");
        }

        // Declare a new vector to store the result
        float[] result = new float[vector1.length];

        // Add the vectors element-wise
        for (int i = 0; i < vector1.length; i++) {
            result[i] = vector1[i] + vector2[i];
        }

        // Return the result
        return result;
    }


    // Method to subtract two matrices
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

    // Method to subtract two vectors
    public static float[] subtract(float[] vector1, float[] vector2) {
        // Check that the vectors have the same size
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must have the same size");
        }

        // Declare a new vector to store the result
        float[] result = new float[vector1.length];

        // Subtract the vectors element-wise
        for (int i = 0; i < vector1.length; i++) {
            result[i] = vector1[i] - vector2[i];
        }

        // Return the result
        return result;
    }

    // Method to multiply two matrices
    public static float[][] multiply(float[][] matrix1, float[][] matrix2) {
        // Check that the matrices can be multiplied
        if (matrix1[0].length != matrix2.length) {
            throw new IllegalArgumentException("Matrices cannot be multiplied");
        }

        // Declare a new matrix to store the result
        float[][] result = new float[matrix1.length][matrix2[0].length];

        // Multiply the matrices
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                for (int k = 0; k < matrix1[0].length; k++) {
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }

        // Return the result
        return result;
    }


    // Method to multiply a matrix and a vector
    public static float[] multiply(float[][] matrix, float[] vector) {
        // Check that the matrix and vector can be multiplied
        if (matrix[0].length != vector.length) {
            throw new IllegalArgumentException("Matrix and vector cannot be multiplied");
        }

        // Declare a new vector to store the result
        float[] result = new float[matrix.length];

        // Multiply the matrix and vector
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < vector.length; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }

        // Return the result
        return result;
    }

    // Method to transpose a matrix
    public static float[][] transpose(float[][] matrix) {
        // Declare a new matrix to store the result
        float[][] result = new float[matrix[0].length][matrix.length];

        // Transpose the matrix
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[j][i] = matrix[i][j];
            }
        }

        // Return the result
        return result;
    }

    // Method to invert a matrix
    public static float[][] inverse(float[][] matrix) {
        // Check that the matrix is square
        if (matrix.length != matrix[0].length) {
            throw new IllegalArgumentException("Matrix must be square");
        }

        // Declare a new matrix to store the result
        float[][] result = new float[matrix.length][matrix[0].length];

        // Implement the matrix inversion algorithm here

        // Return the result
        return result;
    }
}