package com.routes.analyzer.service;

import com.routes.admin.api.Route;
import com.routes.analyzer.repository.RoutesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RouteService {

    @Autowired
    private RoutesRepository routesRepository;

    public List<Route> getRoutes(String country) {
        return routesRepository.findRoutes(country, LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31));
    }
}
