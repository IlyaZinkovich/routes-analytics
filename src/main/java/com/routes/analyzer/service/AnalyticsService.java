package com.routes.analyzer.service;

import com.routes.admin.api.Route;
import com.routes.analyzer.analytics.Predictor;
import org.datavec.api.records.writer.impl.csv.CSVRecordWriter;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import static java.nio.file.Files.createFile;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;

@Service
public class AnalyticsService {

    private static final String COUNTRIES_LIST_FILE_NAME = "countries.txt";
    private static final String TEST_DATA_DIRECTORY = "testData/";
    private final List<String> countries;

    @Autowired
    private RouteService routeService;

    @Autowired
    private Predictor predictor;

    public AnalyticsService() {
        this.countries = getCountries();
    }

    public List<Route> analyze(String country, LocalDate startDate, LocalDate endDate) {
        List<Route> routes = routeService.getRoutes(country);
        Map<LocalDate, Map<String, Integer>> routesSeries = routes.stream()
                .filter(route -> countries.contains(route.getFromPlace().getCountry()))
                .collect(groupingBy(Route::getDate,
                        toMap(route -> route.getFromPlace().getCountry(), route -> (1), (r1, r2) -> r1 + r2)));
        prepareTestData(country, routesSeries, startDate, endDate);
        predictor.train("testData.csv");
        predictor.test("testData.csv");
        return null;
    }

    private void prepareTestData(String destinationName, Map<LocalDate, Map<String, Integer>> routesSeries,
                                 LocalDate startDate, LocalDate endDate) {
        String testDataFileName = TEST_DATA_DIRECTORY + destinationName;
        deleteOldTestDataFile(testDataFileName);
        File testDataFile = createFileWithName(testDataFileName);
        CSVRecordWriter writer = getWriterForFile(testDataFile);
        writeHeader(writer);
        writeData(routesSeries, startDate, endDate, writer);
    }

    private void writeData(Map<LocalDate, Map<String, Integer>> routesSeries, LocalDate startDate, LocalDate endDate, CSVRecordWriter writer) {
        while (!startDate.equals(endDate)) {
            Map<String, Integer> routesByCountry = routesSeries.get(startDate);
            writeDataRow(startDate, writer, routesByCountry);
            startDate = startDate.plusDays(1);
        }
    }

    private void writeDataRow(LocalDate startDate, CSVRecordWriter writer, Map<String, Integer> routesByCountry) {
        List<Writable> row = new ArrayList<>(asList(new Text(startDate.format(ofPattern("yyyy-MM-dd"))),
                new Text(String.valueOf(startDate.getDayOfWeek().getValue()))));
        row.addAll(getRoutesCountByCountry(routesByCountry));
        writeRow(writer, row);
    }

    private List<Writable> getRoutesCountByCountry(Map<String, Integer> routesByCountry) {
        if (isNull(routesByCountry))
            return range(0, countries.size()).mapToObj(i -> new Text("0")).collect(toList());
        else
            return countries.stream()
                    .map(country -> routesByCountry.containsKey(country) ? routesByCountry.get(country) : 0)
                    .map(count -> new Text(String.valueOf(count))).collect(toList());
    }

    private void writeHeader(CSVRecordWriter writer) {
        List<Writable> header = new ArrayList<>(asList(new Text("Date"), new Text("Day of Week")));
        countries.stream().map(Text::new).forEach(header::add);
        writeRow(writer, header);
    }

    private void deleteOldTestDataFile(String testDataFileName) {
        try {
            Files.deleteIfExists(Paths.get(testDataFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CSVRecordWriter getWriterForFile(File file) {
        try {
            return new CSVRecordWriter(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
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
