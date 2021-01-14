package pgdp.corona;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PandemicAnalysis {

    public static void main(String[] args) {
        String filename = "RKI_COVID19_Example.csv";
        //String dataset = "RKI_COVID19_Darmstadt_Koblenz.csv";
        Dataset dataset = new Dataset("dataset/" + filename);

        dataset.stream().forEach(System.out::println);
    }
}
