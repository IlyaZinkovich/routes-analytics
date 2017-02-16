package com.routes.analytics.data;

import org.datavec.api.records.writer.impl.csv.CSVRecordWriter;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;
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
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

@Service
public class DataGenerator {

    private static final String COUNTRIES_LIST_FILE_NAME = "countries.txt";
    private static final String TEST_DATA_DIRECTORY = "testData/";
    private final List<String> countries = readCountries();

    public File prepareTestData(String destinationName, Map<LocalDate, Map<String, Integer>> routesSeries) {
        File testDataFile = createTestDataFile(destinationName);
        CSVRecordWriter writer = getWriterForFile(testDataFile);
        writeRow(writer, singletonList(new Text("@RELATION routes")));
        writeRow(writer, singletonList(new Text("@ATTRIBUTE date DATE \"yyyy-MM-dd\"")));
        countries.forEach(country ->
                writeRow(writer, singletonList(new Text("@ATTRIBUTE \"" + country + "\" NUMERIC"))));
        writeRow(writer, singletonList(new Text("@DATA")));
        writeData(routesSeries, writer);
        return testDataFile;
    }

    public List<String> getAvailableCountries() {
        return countries;
    }

    private File createTestDataFile(String destinationName) {
        String testDataFileName = TEST_DATA_DIRECTORY + destinationName;
        deleteOldTestDataFile(testDataFileName);
        return createFileWithName(testDataFileName);
    }

    private void writeData(Map<LocalDate, Map<String, Integer>> routesSeries, CSVRecordWriter writer) {
        routesSeries.keySet().stream().sorted().forEach(date -> writeRoutesForGivenDate(routesSeries, writer, date));
    }

    private List<Writable> getRoutesCountByCountry(Map<String, Integer> routesByCountry) {
        if (isNull(routesByCountry))
            return range(0, countries.size()).mapToObj(i -> new Text("0")).collect(toList());
        else
            return countries.stream()
                    .map(country -> routesByCountry.containsKey(country) ? routesByCountry.get(country) : 0)
                    .map(count -> new Text(String.valueOf(count))).collect(toList());
    }

    private void writeRoutesForGivenDate(Map<LocalDate, Map<String, Integer>> routesSeries,
                                         CSVRecordWriter writer, LocalDate date) {
        Map<String, Integer> routesByCountry = routesSeries.get(date);
        writeDataRow(date, writer, routesByCountry);
    }

    private void writeDataRow(LocalDate startDate, CSVRecordWriter writer, Map<String, Integer> routesByCountry) {
        List<Writable> row = new ArrayList<>(singletonList(new Text(startDate.format(ofPattern("yyyy-MM-dd")))));
        row.addAll(getRoutesCountByCountry(routesByCountry));
        writeRow(writer, row);
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

    private List<String> readCountries() {
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
