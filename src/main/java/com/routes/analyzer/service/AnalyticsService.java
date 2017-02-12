package com.routes.analyzer.service;

import com.routes.admin.api.Route;
import com.routes.analyzer.analytics.Predictor;
import org.datavec.api.records.writer.impl.csv.CSVRecordWriter;
import org.datavec.api.writable.IntWritable;
import org.datavec.api.writable.Writable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Month;
import java.util.*;

import static java.nio.file.Files.createFile;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class AnalyticsService {

    private static final String COUNTRIES_LIST_FILE_NAME = "countries.txt";
    private final List<String> countries;

    @Autowired
    private RouteService routeService;

    @Autowired
    private Predictor predictor;

    public AnalyticsService() {
        this.countries = getCountries();
    }

    public List<Route> analyze(String country) {
        List<Route> routes = routeService.getRoutes(country);
        Map<String, List<Route>> routesByOriginCountry = routes.stream()
                .filter(route -> countries.contains(route.getFromPlace().getCountry()))
                .collect(groupingBy(route -> route.getFromPlace().getCountry(), toList()));
        prepareTestData(country, routesByOriginCountry);
        predictor.train("testData.csv");
        predictor.test("testData.csv");
        return null;
    }

    private void prepareTestData(String destinationCountry, Map<String, List<Route>> routesByOriginCountry) {
        String testDataFileName = "testData.csv";
        File testDataFile = createFileWithName(testDataFileName);
        CSVRecordWriter writer = getWriterForFile(testDataFile);
        routesByOriginCountry.keySet().forEach(country ->
                    writeRoutesForCountry(destinationCountry, routesByOriginCountry, writer, country));
    }

    private CSVRecordWriter getWriterForFile(File file) {
        try {
            return new CSVRecordWriter(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeRoutesForCountry(String destinationCountry, Map<String, List<Route>> routesByOriginCountry, CSVRecordWriter writer, String country) {
        List<Writable> row = new ArrayList<>();
        row.add(new IntWritable(countries.indexOf(country)));
        Map<Integer, List<Route>> routesPerMonth = routesByOriginCountry.get(country).stream()
                .collect(groupingBy(route -> route.getDate().getMonthValue()));
        stream(Month.values())
                .map(Month::getValue)
                .map(month -> routesPerMonth.containsKey(month) ? routesPerMonth.get(month) : emptyList())
                .map(List::size)
                .forEach(count -> row.add(new IntWritable(count)));
        writeRow(writer, row);
    }

    private void writeRow(CSVRecordWriter writer, List<Writable> row) {
        try {
            writer.write(row);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File createFileWithName(String fileName) {
        try {
            return createFile(Paths.get(fileName)).toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getCountries() {
        try (Scanner scanner = new Scanner(new File(COUNTRIES_LIST_FILE_NAME))) {
            return readCountries(scanner).stream().sorted().collect(toList());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> readCountries(Scanner scanner) {
        Set<String> countries = new HashSet<>();
        while (scanner.hasNextLine()) {
            countries.add(scanner.nextLine());
        }
        return countries;
    }
}
