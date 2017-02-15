package com.routes.analyzer.controller;

import com.routes.analyzer.model.Prediction;
import com.routes.analyzer.service.AnalyticsService;
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
