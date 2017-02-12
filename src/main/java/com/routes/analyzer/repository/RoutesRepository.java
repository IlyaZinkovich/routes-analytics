package com.routes.analyzer.repository;

import com.routes.admin.api.Place;
import com.routes.admin.api.Route;
import com.routes.analyzer.model.PlaceNode;
import com.routes.analyzer.model.RouteRelationship;
import org.apache.commons.io.FileUtils;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Paths.get;
import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Repository
public class RoutesRepository {

    @Autowired
    private Neo4jOperations neo4jTemplate;

    private Set<String> countries;

    public RoutesRepository() {
        this.countries = getCountries();
    }

//    @PostConstruct
//    public void getRoutes() {
//        Set<String> countries = getCountries();
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("country", "Germany");
//        Result results = neo4jTemplate.query("MATCH (from)-[route]->(to {country: {country}}) " +
//                "WHERE from.country <> {country} RETURN from, route", parameters);
//        Map<String, List<LocalDate>> routeDates = new HashMap<>();
//        results.forEach(result -> {
//            Info info = extractInformation(result);
//            if (info != null && countries.contains(info.getFrom())) {
//                routeDates.putIfAbsent(info.getFrom(), new ArrayList<>());
//                routeDates.get(info.getFrom()).add(info.getDate());
//            }
//        });
//        makeOutput(routeDates);
//    }

//    private void makeOutput(Map<String, List<LocalDate>> routeDates) {
//        String directoryName = "output";
//        deleteDirectory(directoryName);
//        createOutputDirectory(directoryName);
//        String fileName = directoryName + "/" + "Germany";
//        createFileWithName(fileName);
//        try (PrintWriter writer = new PrintWriter(new File(fileName))){
//            routeDates.keySet().forEach(country -> {
//                List<LocalDate> dates = routeDates.get(country);
//                Collections.sort(dates);
//                Map<Integer, Integer> routesPerMonth = new HashMap<>();
//                stream(Month.values())
//                    .map(Month::getValue)
//                    .forEach(month -> routesPerMonth.put(month, 0));
//                dates.forEach(date -> routesPerMonth.put(date.getMonthValue(), routesPerMonth.get(date.getMonthValue()) + 1));
////                writer.write(country);
////                writer.write(",");
//                String routesByMonthString = stream(Month.values())
//                    .map(Month::getValue)
//                    .map(routesPerMonth::get)
//                    .map(Objects::toString)
//                    .collect(joining(","));
//                writer.write(routesByMonthString);
//                writer.write("\n");
//            });
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private void deleteDirectory(String directoryName) {
        try {
            FileUtils.deleteDirectory(new File(directoryName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createOutputDirectory(String directoryName) {
        try {
            createDirectories(get(directoryName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createFileWithName(String fileName) {
        try {
            createFile(Paths.get(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Info extractInformation(Map<String, Object> result) {
        Optional<LocalDate> optionalDate = ((RelationshipModel) result.get("route"))
                .getPropertyList()
                .stream()
                .filter(property -> "date".equals(property.getKey()))
                .findFirst()
                .map(Property::getValue)
                .map(Object::toString)
                .map(LocalDate::parse);
        Optional<String> optionalFrom = ((NodeModel) result.get("from")).getPropertyList()
                .stream()
                .filter(property -> "country".equals(property.getKey()))
                .findFirst()
                .map(Property::getValue)
                .map(Object::toString);
        if (optionalDate.isPresent() && optionalFrom.isPresent()) {
            String from = optionalFrom.get();
            LocalDate date = optionalDate.get();
            return new Info(from, date);
        }
        return null;
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
