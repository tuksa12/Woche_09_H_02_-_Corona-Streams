package pgdp.corona;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class PandemicAnalysis {

    public static void main(String[] args) {
        String filename = "RKI_COVID19_example.csv";
        //String filename = "RKI_COVID19_Darmstadt_Koblenz.csv";
        Dataset dataset = new Dataset("dataset/" + filename);

        dataset.stream().forEach(System.out::println);
        System.out.println(totalCases(dataset.stream(), Entry::getInfection));
        System.out.println(newCases(dataset.stream(), Entry::getInfection));
        System.out.println(activeInfections(dataset.stream()));
        System.out.println(safestStates(dataset.stream()));
        System.out.println(firstDate(dataset.stream()));
        System.out.println(newInfectionsByDate(dataset.stream()));
        System.out.println(dailyNewInfections(dataset.stream()));
        System.out.println(avgDailyNewInfections(dataset.stream()));
    }
    //Lembrar de testar todos esses metodos
    public static int totalCases(Stream<Entry> entryStream, CaseReport.Getter getter){
        return entryStream
            .filter(x -> getter.get(x).type.equals(CaseReport.Type.NEW) || getter.get(x).type.equals(CaseReport.Type.CORRECTION)|| getter.get(x).type.equals(CaseReport.Type.NOT_NEW))
            .mapToInt(x -> getter.get(x).count)
                .sum();
    }

    public static int newCases(Stream<Entry> entryStream, CaseReport.Getter getter){
        return entryStream
                .filter(x -> getter.get(x).type.equals(CaseReport.Type.NEW) || getter.get(x).type.equals(CaseReport.Type.CORRECTION))
                .mapToInt(x -> getter.get(x).count)
                .sum();
    }

    public static int activeInfections(Stream<Entry> entryStream){
        return entryStream
            .filter(x -> x.getRecovery().type.equals(CaseReport.Type.NEW) || x.getInfection().type.equals(CaseReport.Type.NEW))
                .mapToInt(x -> x.getInfection().count - x.getRecovery().count)
                .sum();
    }

    static int index = -1;

//    public static List<String> safestStates(Stream<Entry> entryStream){
//        List result  =  entryStream
//                .filter(x -> x.getInfection().type.equals(CaseReport.Type.NEW) ||  x.getInfection().type.equals(CaseReport.Type.NOT_NEW))
//                .collect(Collectors.groupingBy(Entry::getState,Collectors.summingInt(x -> x.getInfection().count)))
//                .entrySet()
//                .stream()
//                .sorted(Comparator.comparing(Map.Entry::getValue))
//                .collect(Collectors.toList());
//        List resultado = new ArrayList<>();
//        for (int i = 0; i <result.size(); i++) {
//            resultado.add(result.get(0));
//        }
//         return resultado;
//    }

    public static List<String> safestStates(Stream<Entry> entryStream){
        List<String> list = new ArrayList<>();
        List<Integer> cases = new ArrayList<>();
        List<String> result = new ArrayList<>();
        entryStream
                .forEach(s -> {
                    if (!list.contains(s.getState())){
                        list.add(s.getState());
                        result.add(s.getState());
                        cases.add(s.getInfection().count);
                        index ++;
                    }else{
                        cases.set(index,cases.get(index)+s.getInfection().count-s.getRecovery().count);
                    }
                });
        list.stream()
                .forEach(x -> {
                    if(cases.size() > list.indexOf(x) + 1){//Adicionar for loop para checar todos os outros estados com o proximo,Bayern sai no lugar certo mas sachsen nao
                         if(cases.get(list.indexOf(x)) > cases.get(list.indexOf(x)+1)){
                             String helper = result.get(list.indexOf(x));
                             result.set(list.indexOf(x),list.get(list.indexOf(x)+1));
                             result.set(list.indexOf(x)+1,helper);
                         }
                    }
                });
        return result;
    }

    public static LocalDate firstDate(Stream<Entry> entryStream){
         return entryStream
                .map(Entry::getReportingDate)
                .sorted()
                .findFirst()
                .orElse(null);
    }

    public static Map<LocalDate, Integer> newInfectionsByDate(Stream<Entry> entryStream){
        Map<LocalDate, Integer> map = entryStream
                .filter(x -> x.getInfection().type.equals(CaseReport.Type.NEW) || x.getInfection().type.equals(CaseReport.Type.NOT_NEW))
                .collect(Collectors.groupingBy(x-> x.getReportingDate(),
                        Collectors.summingInt(x -> x.getInfection().count)));

        Map<LocalDate, Integer> sort = new TreeMap<LocalDate, Integer>(map);
        return sort;
    }

    public static int[] dailyNewInfections(Stream<Entry> entryStream){
           return entryStream
                    .filter(x -> x.getInfection().type.equals(CaseReport.Type.NEW))
                    .mapToInt(x -> x.getInfection().count)
                    .toArray();
    }

    public static double avgDailyNewInfections(Stream<Entry> entryStream){
        return entryStream
                .filter(x -> x.getInfection().type.equals(CaseReport.Type.NEW) )
                .collect(Collectors.groupingBy(Entry::getReportingDate,Collectors.summingInt(value -> value.getInfection().count)))
                .values()
                .stream()
                .mapToInt(x -> x.intValue())
                .count();
    }

    public static Map<String, Map<Character, Integer>> totalCasesBySexByAgeGroup(Stream<Entry> entryStream, CaseReport.Getter getter){
//        return entryStream
//                .filter(x -> getter.get(x).type.equals(CaseReport.Type.NEW) || getter.get(x).type.equals(CaseReport.Type.CORRECTION)|| getter.get(x).type.equals(CaseReport.Type.NOT_NEW))
//                .map(x->x.)

    return null;
    }


}
