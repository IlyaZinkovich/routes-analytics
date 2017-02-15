package com.routes.analyzer.service;

import com.routes.admin.api.Route;
import com.routes.analyzer.analytics.TimeSeriesPredictor;
import com.routes.analyzer.model.Prediction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
public class AnalyticsService {

    @Autowired
    private RouteService routeService;

    @Autowired
    private TestDataPreparer testDataPreparer;

    public List<Prediction> predict(String country, Integer predictedDays) {
        List<Route> realRoutes = routeService.getRoutes(country);
        List<String> availableCountries = testDataPreparer.getAvailableCountries();
        Map<LocalDate, Map<String, Integer>> routesSeries = realRoutes.stream()
                .filter(route -> availableCountries.contains(route.getFromPlace().getCountry()))
                .collect(groupingBy(Route::getDate,
                        toMap(route -> route.getFromPlace().getCountry(), route -> (1), (r1, r2) -> r1 + r2)));
        File testDataFile = testDataPreparer.prepareTestData(country, routesSeries);
        TimeSeriesPredictor predictor = new TimeSeriesPredictor();
        return predictor.predict(testDataFile, availableCountries, predictedDays);
    }
}
