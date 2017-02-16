package com.routes.analytics.interactors;

import com.routes.analytics.clients.RouteServiceClient;
import com.routes.analytics.data.DataGenerator;
import com.routes.analytics.entities.Route;
import com.routes.analytics.predictors.TimeSeriesPredictor;
import com.routes.analytics.entities.Prediction;
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
    private RouteServiceClient routeServiceClient;

    @Autowired
    private DataGenerator dataGenerator;

    public List<Prediction> predict(String country, Integer predictedDays) {
        List<Route> realRoutes = routeServiceClient.getRoutes(country);
        List<String> availableCountries = dataGenerator.getAvailableCountries();
        Map<LocalDate, Map<String, Integer>> routesSeries = realRoutes.stream()
                .filter(route -> availableCountries.contains(route.getFrom().getCountry()))
                .collect(groupingBy(Route::getDate,
                        toMap(route -> route.getFrom().getCountry(), route -> (1), (r1, r2) -> r1 + r2)));
        File testDataFile = dataGenerator.prepareTestData(country, routesSeries);
        TimeSeriesPredictor predictor = new TimeSeriesPredictor();
        return predictor.predict(testDataFile, availableCountries, predictedDays);
    }
}
