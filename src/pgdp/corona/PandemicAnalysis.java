package pgdp.corona;

import java.time.LocalDate;
import java.time.Period;
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
        System.out.println(Arrays.toString(dailyNewInfections(dataset.stream())));
        System.out.println(avgDailyNewInfections(dataset.stream()));
        System.out.println(totalCasesBySexByAgeGroup(dataset.stream(),Entry::getInfection));
    }
    //First 3 methods are similar in the structure, filter the type -> mapToInt -> sum
    public static int totalCases(Stream<Entry> entryStream, CaseReport.Getter getter){
        return entryStream
                .filter(x -> getter.get(x).type.equals(CaseReport.Type.NEW) || getter.get(x).type.equals(CaseReport.Type.NOT_NEW))
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
                .filter(x -> !x.getInfection().type.equals(CaseReport.Type.CORRECTION) && !x.getRecovery().type.equals(CaseReport.Type.CORRECTION)
                        && !x.getDeath().type.equals(CaseReport.Type.CORRECTION))
                .mapToInt(x -> x.getInfection().count - x.getRecovery().count - x.getDeath().count)
                .sum();
    }

    public static List<String> safestStates(Stream<Entry> entryStream){
        //Map that groups each state to their specific number of infections
        Map<String, Integer> map = new HashMap<>();
        entryStream
                .filter(x -> !x.getState().isEmpty() || !x.getInfection().type.equals(CaseReport.Type.CORRECTION) && !x.getRecovery().type.equals(CaseReport.Type.CORRECTION)
                        && !x.getDeath().type.equals(CaseReport.Type.CORRECTION))
                .forEach(x -> {//I tried multiple ways to not use forEach but this is the only way I could manage to do it
                    if(map.containsKey(x.getState())){//Condition to add the infections to each state
                        int oldValue = map.get(x.getState());
                        map.replace(x.getState(),oldValue + x.getInfection().count-x.getRecovery().count -x.getDeath().count);
                    }else{
                        map.put(x.getState(),x.getInfection().count-x.getRecovery().count -x.getDeath().count);
                    }
                });
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    public static LocalDate firstDate(Stream<Entry> entryStream){
        //Straight forward stream to find the first date
         return entryStream
                .map(Entry::getReportingDate)
                .sorted()
                .findFirst()
                .orElse(null);
    }

    public static Map<LocalDate, Integer> newInfectionsByDate(Stream<Entry> entryStream){
        //Stream to build a map of dates and their infections count
        Map<LocalDate, Integer> map = entryStream
                .filter(x -> x.getInfection().type.equals(CaseReport.Type.NEW) || x.getInfection().type.equals(CaseReport.Type.NOT_NEW))
                .collect(Collectors.groupingBy(dates-> dates.getReportingDate(),
                        Collectors.summingInt(count -> count.getInfection().count)));
        //TreeMap that sorts from earliest date to latest date
        Map<LocalDate, Integer> sort = new TreeMap<LocalDate, Integer>(map);
        return sort;
    }

    public static int[] dailyNewInfections(Stream<Entry> entryStream){
        //Similar structure to the newInfectionsByDate
        Map<LocalDate, Integer> map = entryStream
                .collect(Collectors.groupingBy(dates-> dates.getReportingDate(),
                        Collectors.summingInt(count -> {//Conditions to add the infection count to NEW type or 0 to other types
                            if(count.getInfection().type.equals(CaseReport.Type.NEW)){
                                return count.getInfection().count;
                            }else{
                                return 0;
                            }
                        })));
        Map<LocalDate, Integer> sort = new TreeMap<LocalDate, Integer>(map);//Sorting the map chronologically

        //With the sorted by date map, I transformed the days into an array, to easily use the first and last date
        Object[] dates = sort.keySet().stream().toArray();
        LocalDate firstDate = (LocalDate) dates[0];
        LocalDate lastDate = (LocalDate) dates[dates.length-1];

        //For loop to put the days that are missing from the map
        for (LocalDate i = firstDate; i.isBefore(lastDate); i = i.plusDays(1)) {
            if(!sort.containsKey(i))
                sort.put(i,0);
        }
        return sort.values().stream().mapToInt(i -> i).toArray();
    }


    public static double avgDailyNewInfections(Stream<Entry> entryStream){
        //Use of the method newInfectionsByDate to create a sorted map of infections each day
        Map<LocalDate, Integer> infectionsByDate = newInfectionsByDate(entryStream);

        //With the sorted by date map, I transformed the days into an array, to easily use the first and last date
        Object[] dates =  infectionsByDate.keySet().toArray();
        LocalDate firstDate = (LocalDate) dates[0];
        LocalDate lastDate = (LocalDate) dates[dates.length-1];

        //Using firstDate.until(lastDate), p is the period between the first and the last date(exclusive)
        Period p = firstDate.until(lastDate);

        double days = p.getDays()+1;//+1 includes the excluded last day
        double infections = infectionsByDate.values().stream().mapToInt(x->x.intValue()).sum();//Sums the infections

    return infections/days;
    }

    //I tried working on this method multiple ways but i wasn't able to make it work, so I commented out
    public static Map<String, Map<Character, Integer>> totalCasesBySexByAgeGroup(Stream<Entry> entryStream, CaseReport.Getter getter){
        Map<String, Map<Character, Integer>> map = new HashMap<>();
//        Map<Character, Integer> secondMap = new HashMap<>();
//
//        entryStream
//                .filter(x -> getter.get(x).type.equals(CaseReport.Type.NEW) || getter.get(x).type.equals(CaseReport.Type.NOT_NEW))
//                .forEach(x -> {
//                    if(map.containsKey(x.getAgeGroup())){
//                        if(secondMap.containsKey(x.getSex())){
//                            int previousAmount = secondMap.get(x.getSex());
//                            secondMap.replace(x.getSex(),getter.get(x).count + previousAmount);
//                        } else{
//                            secondMap.put(x.getSex(),getter.get(x).count);
//                        }
//                        map.replace(x.getAgeGroup(),secondMap);
//                    } else{
//                        secondMap.clear();
//                        secondMap.put(x.getSex(),getter.get(x).count);
//                        map.put(x.getAgeGroup(),secondMap);
//
//                    }
//                        });
//        return map;
        return null;
    }

}
