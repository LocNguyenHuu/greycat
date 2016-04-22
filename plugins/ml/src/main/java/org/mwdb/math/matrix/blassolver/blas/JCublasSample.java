package org.mwdb.math.matrix.blassolver.blas;

/* Imports, JCublas */
import jcuda.*;
import jcuda.jcublas.*;

//TODO delete this
class JCublasSample
{
    /* Matrix size */
    private static final int N = 5000;

    /* Main */
    public static void main(String args[])
    {
        double h_A[];
        double h_B[];
        double h_C[];
        Pointer d_A = new Pointer();
        Pointer d_B = new Pointer();
        Pointer d_C = new Pointer();
        double alpha = 1.0d;
        double beta = 0.0d;
        int n2 = N * N;
        int i;

    /* Initialize JCublas */
        JCublas.cublasInit();

    /* Allocate host memory for the matrices */
        h_A = new double[n2];
        h_B = new double[n2];
        h_C = new double[n2];

    /* Fill the matrices with test data */
        for (i = 0; i < n2; i++)
        {
            h_A[i] = (float)Math.random();
            h_B[i] = (float)Math.random();
            h_C[i] = (float)Math.random();
        }

    /* Allocate device memory for the matrices */
        JCublas.cublasAlloc(n2, Sizeof.DOUBLE, d_A);
        JCublas.cublasAlloc(n2, Sizeof.DOUBLE, d_B);
        JCublas.cublasAlloc(n2, Sizeof.DOUBLE, d_C);

    /* Initialize the device matrices with the host matrices */
        JCublas.cublasSetVector(n2, Sizeof.DOUBLE, Pointer.to(h_A), 1, d_A, 1);
        JCublas.cublasSetVector(n2, Sizeof.DOUBLE, Pointer.to(h_B), 1, d_B, 1);
        JCublas.cublasSetVector(n2, Sizeof.DOUBLE, Pointer.to(h_C), 1, d_C, 1);

    /* Performs operation using JCublas */
        JCublas.cublasDgemm('n', 'n', N, N, N, alpha, d_A, N, d_B, N, beta, d_C, N);

    /* Read the result back */
        JCublas.cublasGetVector(n2, Sizeof.DOUBLE, d_C, 1, Pointer.to(h_C), 1);

    /* Memory clean up */



        JCublas.cublasFree(d_A);
        JCublas.cublasFree(d_B);
        JCublas.cublasFree(d_C);

    /* Shutdown */
        JCublas.cublasShutdown();

    }
}