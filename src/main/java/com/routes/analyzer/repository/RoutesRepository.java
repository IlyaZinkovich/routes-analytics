package com.routes.analyzer.repository;

import com.routes.admin.api.Place;
import com.routes.admin.api.Route;
import com.routes.analyzer.model.PlaceNode;
import com.routes.analyzer.model.RouteRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.toList;

@Repository
public class RoutesRepository {

    @Autowired
    private Neo4jOperations neo4jTemplate;

    private Set<String> countries;

    public RoutesRepository() {
        this.countries = getCountries();
    }

    private Set<String> getCountries() {
        try {
            Scanner scanner = new Scanner(new File("countries.txt"));
            Set<String> countries = new HashSet<>();
            while (scanner.hasNextLine()) {
                countries.add(scanner.nextLine());
            }
            return countries;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Route> findRoutes(String country, LocalDate after, LocalDate before) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("country", country);
        parameters.put("after", after.format(ofPattern("yyyy-MM-dd")));
        parameters.put("before", before.format(ofPattern("yyyy-MM-dd")));
        Iterable<RouteRelationship> routes = neo4jTemplate.queryForObjects(RouteRelationship.class,
                "MATCH (from)-[r:ROUTE]->(to {country: {country}}) " +
                        "WHERE r.date >= {after} AND r.date <= {before} RETURN r", parameters);
        return StreamSupport.stream(routes.spliterator(), false).map(this::convert).collect(toList());
    }

    private Route convert(RouteRelationship routeRelationship) {
        PlaceNode from = routeRelationship.getFrom();
        PlaceNode to = routeRelationship.getTo();
        Place fromPlace = new Place(from.getCity(), from.getCountry(), from.getLatitude(), from.getLongitude());
        Place toPlace = new Place(to.getCity(), to.getCountry(), to.getLatitude(), to.getLongitude());
        Route route = new Route(fromPlace, toPlace,
                parse(routeRelationship.getDate()), routeRelationship.getSource());
        route.setId(routeRelationship.getRelationshipId());
        return route;
    }
}
