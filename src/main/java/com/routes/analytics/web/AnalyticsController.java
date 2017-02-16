package com.routes.analytics.web;

import com.routes.analytics.entities.Prediction;
import com.routes.analytics.interactors.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping(path = "/analytics/routes")
    public List<Prediction> analyzeRoutes(@RequestParam(name = "country") String country,
                                          @RequestParam(name = "predictedDays") Integer predictedDays) {
        return analyticsService.predict(country, predictedDays);
    }
}
