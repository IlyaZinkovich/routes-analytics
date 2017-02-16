package com.routes.analytics.predictors;

import com.routes.analytics.entities.Prediction;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Instances;
import weka.filters.supervised.attribute.TSLagMaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class TimeSeriesPredictor {

    private final WekaForecaster forecaster;

    public TimeSeriesPredictor() {
        this.forecaster = new WekaForecaster();
        this.forecaster.setBaseForecaster(new GaussianProcesses());
        TSLagMaker tsLagMaker = this.forecaster.getTSLagMaker();
        tsLagMaker.setTimeStampField("date");
        tsLagMaker.setAddDayOfWeek(true);
        tsLagMaker.setAddMonthOfYear(true);
        tsLagMaker.setAddWeekendIndicator(true);
        tsLagMaker.setAddQuarterOfYear(true);
    }

    public List<Prediction> predict(File testDataFile, List<String> countries, Integer predictedDays) {
        Instances timeSeries = readTimeSeries(testDataFile);
        return countries.stream()
                .flatMap(country -> predictRoutesForCountry(predictedDays, timeSeries, country))
                .collect(toList());
    }

    private Stream<Prediction> predictRoutesForCountry(Integer predictedDays, Instances timeSeries, String country) {
        try {
            trainForecaster(timeSeries, country);
            LocalDate dateBeforePrediction = getDateBeforePrediction(forecaster);
            List<List<NumericPrediction>> forecast = forecaster.forecast(predictedDays, System.out);
            return IntStream.range(0, predictedDays).mapToObj(i -> getPrediction(country, dateBeforePrediction, forecast, i));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LocalDate getDateBeforePrediction(WekaForecaster trainedForecaster) throws Exception {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli((long) trainedForecaster.getTSLagMaker().getCurrentTimeStampValue()),
                TimeZone.getDefault().toZoneId()).toLocalDate();
    }

    private void trainForecaster(Instances timeSeries, String country) throws Exception {
        forecaster.setFieldsToForecast(country);
        forecaster.buildForecaster(timeSeries, System.out);
        forecaster.primeForecaster(timeSeries);
    }

    private Prediction getPrediction(String country, LocalDate localDate, List<List<NumericPrediction>> forecast,
                                     int nextDaysCount) {
        LocalDate date = localDate.plusDays(nextDaysCount);
        Double predictedRoutesCount = forecast.get(nextDaysCount).get(0).predicted();
        return new Prediction(date, country, predictedRoutesCount);
    }

    private Instances readTimeSeries(File testDataFile) {
        try {
            return new Instances(new BufferedReader(new FileReader(testDataFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
