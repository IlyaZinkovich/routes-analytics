package com.routes.analyzer.analytics;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import static org.deeplearning4j.nn.api.OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT;
import static org.deeplearning4j.nn.conf.Updater.NESTEROVS;
import static org.deeplearning4j.nn.weights.WeightInit.XAVIER;
import static org.nd4j.linalg.activations.Activation.IDENTITY;
import static org.nd4j.linalg.activations.Activation.TANH;

@Component
public class Predictor {

    private int numInput = 10;
    private int numOutputs = 3;
    private int nHidden = 20;
    private int seed = 123;
    private int iterations = 30;
    private double learningRate = 0.01;
    private double momentum = 0.9;
    private int printIterations = 10;
    private int nEpochs = 20;

    private MultiLayerNetwork network;

    public Predictor() {
        this.network = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .optimizationAlgo(STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .weightInit(XAVIER)
                .updater(NESTEROVS).momentum(momentum)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden)
                        .activation(TANH)
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunction.MSE)
                        .activation(IDENTITY)
                        .nIn(nHidden).nOut(numOutputs).build())
                .pretrain(false).backprop(true).build()
        );
        this.network.init();
        this.network.setListeners(new ScoreIterationListener(printIterations));
    }

    public void train(String dataPath) {
        DataSetIterator iterator = loadData(dataPath, 5, 10, 12);
        for (int i = 0; i < nEpochs; i++) {
            iterator.reset();
            network.fit(iterator);
        }
    }

    public void test(String testDataPath) {
        DataSetIterator iterator = loadData(testDataPath, 5, 10, 12);
        RegressionEvaluation evaluation = new RegressionEvaluation(3);
        while (iterator.hasNext()) {
            DataSet t = iterator.next();
            INDArray features = t.getFeatureMatrix();
            INDArray labels = t.getLabels();
            INDArray predicted = network.output(features, false);
            evaluation.eval(labels, predicted);
        }
        System.out.println(evaluation.stats());
    }

    private DataSetIterator loadData(String dataPath, int batchSize, int labelIndexFrom, int labelIndexTo) {
        RecordReader rr = new CSVRecordReader();
        initializeRecordReader(dataPath, rr);
        return new RecordReaderDataSetIterator(rr, batchSize, labelIndexFrom, labelIndexTo, true);
    }

    private void initializeRecordReader(String dataPath, RecordReader rr) {
        try {
            rr.initialize(new FileSplit(new File(dataPath)));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

//    public static void main(String[] args) {
//        Predictor predictor = new Predictor();
//        predictor.train("output/Germany");
//        predictor.test("output/Germany");
//    }
}
