package com.routes.analyzer.analytics;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;

public class MLPLinearClassifier {
//    public static void main(String[] args) throws Exception {
//        RecordReader rr = new CSVRecordReader();
//        rr.initialize(new FileSplit(new File("output/Germany")));
//        DataSetIterator dataSetIterator = new RecordReaderDataSetIterator(rr, 5, 9, 11,true);
//
//        RecordReader testRR = new CSVRecordReader();
//        testRR.initialize(new FileSplit(new File("output/Germany")));
//        DataSetIterator testDataSetIterator = new RecordReaderDataSetIterator(testRR, 5, 9, 11,true);
//
//        dataSetIterator.forEachRemaining(System.out::println);
//
//        int numInput = 9;
//        int numOutputs = 3;
//        int nHidden = 20;
//        int seed = 123;
//        int iterations = 30;
//        double learningRate = 0.01;
//        double momentum = 0.9;
//        int nEpochs = 20;
//
//        MultiLayerNetwork network = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
//                .seed(seed)
//                .iterations(iterations)
//                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
//                .learningRate(learningRate)
//                .weightInit(WeightInit.XAVIER)
//                .updater(Updater.NESTEROVS).momentum(momentum)
//                .list()
//                .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden)
//                        .activation(Activation.TANH)
//                        .build())
//                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
//                        .activation(Activation.IDENTITY)
//                        .nIn(nHidden).nOut(numOutputs).build())
//                .pretrain(false).backprop(true).build()
//        );
//
//        network.init();
//        network.setListeners(new ScoreIterationListener(10));
//
//
//        for (int i = 0; i < nEpochs; i++) {
//            dataSetIterator.reset();
//            network.fit(dataSetIterator);
//        }
//
//        RegressionEvaluation evaluation = new RegressionEvaluation(3);
//        while (testDataSetIterator.hasNext()) {
//            DataSet t = testDataSetIterator.next();
//            INDArray features = t.getFeatureMatrix();
//            INDArray labels = t.getLabels();
//            INDArray predicted = network.output(features, false);
//            evaluation.eval(labels, predicted);
//        }
//        System.out.println(evaluation.stats());
//    }
}
