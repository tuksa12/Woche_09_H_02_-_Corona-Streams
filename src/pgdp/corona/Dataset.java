package pgdp.corona;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//A Dataset represents a specific RKI COVID-19 data
public class Dataset {
    private final List<Entry> data;
    private static final Map<Integer, String> lookupState = new HashMap<>();
    private static final Map<Integer, String> lookupDistrict = new HashMap<>();
    private static final Map<Integer, Integer> districtIdToStateId = new HashMap<>();


    public Dataset(String filepath) {
        System.out.print("Parsing \"" + filepath + "\"...");
        long before = System.currentTimeMillis();

        data = parseCsvFile(new File(filepath));

        System.out.println(" Done after " + (System.currentTimeMillis() - before) + "ms");
    }

    public Stream<Entry> stream() {
        return data.stream();
    }


    public static String nameOfState(int stateId) {
        return lookupState.get(stateId);
    }

    public static int idOfState(String state) {
        return lookupState.entrySet().stream()
                .filter(entry -> state.equals(entry.getValue()))
                .mapToInt(Map.Entry::getKey)
                .findAny().orElse(-1);
    }

    public static String nameOfDistrict(int districtId) {
        return lookupDistrict.get(districtId);
    }

    public static int idOfDistrict(String district) {
        return lookupDistrict.entrySet().stream()
                .filter(entry -> district.equals(entry.getValue()))
                .mapToInt(Map.Entry::getKey)
                .findAny().orElse(-1);
    }

    public static int stateOfDistrict(int districtId) {
        return districtIdToStateId.getOrDefault(districtId, -1);
    }

    //Given a CSV file, attempt to parse it into a list of Entry objects
    private static List<Entry> parseCsvFile(final File csvFile) {
        try {
            return Files.lines(csvFile.toPath()).skip(1) // skip header of csv file
                    .map(Dataset::lineToEntry) // convert line to Entry object
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("File " + csvFile + " not found");
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Error while parsing file " + csvFile + ": " + e);
        }
    }

    //Given string of a csv row, returns a corresponding Entry object
    private static Entry lineToEntry(String line) {
        final String[] fields = line.split(",");
        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        final int stateId = Integer.parseInt(fields[1]);
        final int districtId = Integer.parseInt(fields[9]);
        final String ageGroup = fields[4];
        final char sex = fields[5].equals("unbekannt") ? 'u' : fields[5].charAt(0);
        final LocalDate reportingDate = LocalDate.parse(fields[8].split(" ")[0], dateFormat);
        final CaseReport infection =
                new CaseReport(Integer.parseInt(fields[6]), CaseReport.Type.fromDataset(Integer.parseInt(fields[12])));
        final CaseReport death =
                new CaseReport(Integer.parseInt(fields[7]), CaseReport.Type.fromDataset(Integer.parseInt(fields[13])));
        final CaseReport recovery =
                new CaseReport(Integer.parseInt(fields[16]), CaseReport.Type.fromDataset(Integer.parseInt(fields[15])));

        //save state/district names in lookup table
        lookupState.putIfAbsent(stateId, fields[2]);
        lookupDistrict.putIfAbsent(districtId, fields[3]);
        districtIdToStateId.putIfAbsent(districtId, stateId);

        return new Entry(districtId, ageGroup, sex, reportingDate, infection, death, recovery);
    }
}
