/**
 * Copyright 2017 DataThings - All rights reserved.
 */
package greycatBlasTest;

import greycat.*;
import greycat.blas.BlasPlugin;
import greycat.ml.MLPlugin;
import greycat.ml.neuralnet.NeuralNet;
import greycat.ml.neuralnet.activation.Activations;
import greycat.ml.neuralnet.layer.Layers;
import greycat.ml.neuralnet.loss.Losses;
import greycat.ml.neuralnet.optimiser.Optimisers;
import greycat.struct.DMatrix;
import greycat.struct.EStructArray;
import greycat.struct.matrix.MatrixOps;
import greycat.struct.matrix.RandomGenerator;
import greycat.struct.matrix.VolatileDMatrix;
import org.junit.Test;

/**
 * @ignore ts
 */
public class VectorizedNN {

    @Test
    public void vectorize() {
        Graph g = GraphBuilder.newBuilder()
                .withPlugin(new MLPlugin())
                .withPlugin(new BlasPlugin())
                .build();
        g.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //set number of input to outputDimensions
                int inputdim = 5;
                int outputdim = 2;

                //number of training set to generate
                int trainset = 500;
                int rounds = 100;

                double learningrate = 0.1;
                double regularisation = 0;
                boolean display=false;
                RandomGenerator randomGenerator=new RandomGenerator();
                randomGenerator.setSeed(1234);

                DMatrix inputs = VolatileDMatrix.random(inputdim, trainset, randomGenerator, -1, 1);
                DMatrix linearsys = VolatileDMatrix.random(outputdim, inputdim, randomGenerator,-2, 2);
                DMatrix outputs = MatrixOps.multiply(linearsys, inputs);
                //System.out.println(outputDimensions.rows() + " , " + outputDimensions.columns());

                Node node1 = g.newNode(0, 0);
                EStructArray egraph1 = (EStructArray) node1.getOrCreate("nn1", Type.ESTRUCT_ARRAY);
                NeuralNet net1 = new NeuralNet(egraph1);
                net1.setRandom(1234, 0.1);
                net1.addLayer(Layers.LINEAR_LAYER, inputdim, outputdim, Activations.LINEAR, null);
                net1.setOptimizer(Optimisers.GRADIENT_DESCENT, new double[]{learningrate/trainset, regularisation}, 1);
                net1.setTrainLoss(Losses.SUM_OF_SQUARES);


                Node node2 = g.newNode(0, 0);
                EStructArray egraph2 = (EStructArray) node2.getOrCreate("nn2", Type.ESTRUCT_ARRAY);
                NeuralNet net2 = new NeuralNet(egraph2);
                net2.setRandom(1234, 0.1);
                net2.addLayer(Layers.LINEAR_LAYER, inputdim, outputdim, Activations.LINEAR, null);
                net2.setOptimizer(Optimisers.GRADIENT_DESCENT, new double[]{learningrate, regularisation}, 0);
                net2.setTrainLoss(Losses.SUM_OF_SQUARES);



                long start=System.currentTimeMillis();
                for (int j = 0; j < rounds; j++) {
                    DMatrix[] err = net1.learnVec(inputs, outputs, true);
                    double[] reserr = Losses.avgLossPerOutput(err[1]);
                    if(display||j==rounds-1) {
                        System.out.print("error Vectorized NN at round " + (j+1) + ": ");
                        for (int i = 0; i < reserr.length; i++) {
                            System.out.print(reserr[i] + " ");
                        }
                        System.out.println("");
                    }
                }
                long end=System.currentTimeMillis();
                System.out.println("time Vectorized: "+(end-start)+" ms");

                System.out.println("");

                start=System.currentTimeMillis();
                for (int j = 0; j < rounds; j++) {
                    double[] lossround=new double[outputdim];
                    for(int i=0;i<trainset;i++){
                        DMatrix[] res= net2.learn(inputs.column(i),outputs.column(i),true);
                        for(int k=0;k<outputdim;k++){
                            lossround[k]+=res[1].get(k,0);
                        }
                    }
                    if(display||j==rounds-1) {
                        System.out.print("error nonVectorized at round " + (j+1) + ": ");
                        for (int k = 0; k < outputdim; k++) {
                            lossround[k] = lossround[k] / trainset;
                            System.out.print(lossround[k] + " ");
                        }
                        System.out.println("");
                    }
                    net2.finalLearn();
                }
                end=System.currentTimeMillis();
                System.out.println("time nonVectorized: "+(end-start)+" ms");



            }
        });


    }

}
