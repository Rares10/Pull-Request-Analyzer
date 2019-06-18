package org.analyzer.pullrequestanalyzer.logic.analyzer;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Service
public class Statistics {

    public double calculateIntegersAverage(List<Integer> values) {

        return values.stream().collect(Collectors.averagingInt(Integer::intValue));
    }

    public double calculateLongsAverage(List<Long> values) {

        return values.stream().collect(Collectors.averagingLong(Long::longValue));
    }

    public double calculateDoublesAverage(List<Double> values) {

        return values.stream().collect(Collectors.averagingDouble(Double::doubleValue));
    }

    public double calculateWeeklyAverage(List<Date> dates) {

        Date firstDate = dates.get(0);
        Date lastDate = dates.get(dates.size() - 1);

        long noOfWeeks = (lastDate.getTime() - firstDate.getTime()) / (1000*60*60*24*7);

        Map<Long, Long> weeks = dates.stream()
                .collect(Collectors.groupingBy(x -> x.getTime() / (1000*60*60*24*7), Collectors.counting()));

        return (double) weeks.values().stream().collect(Collectors.summingLong(Long::longValue)) / noOfWeeks;
    }

    public double calculateDailyAverage(List<Date> dates) {

        Date firstDate = dates.get(0);
        Date lastDate = dates.get(dates.size() - 1);

        long noOfDays = (lastDate.getTime() - firstDate.getTime()) / (1000*60*60*24);

        Map<Long, Long> days = dates.stream()
                .collect(Collectors.groupingBy(x -> x.getTime() / (1000*60*60*24), Collectors.counting()));

        return (double) days.values().stream().collect(Collectors.summingLong(Long::longValue)) / noOfDays;
    }

    public int calculateIntMedianValue(List<Integer> values) {

        if (values.size() > 0)
            return values.get(values.size()/2);
        return 0;
    }

    public long calculateLongMedianValue(List<Long> values) {

        if (values.size() > 0)
            return values.get(values.size()/2);
        return 0;
    }

    public double calculateDoubleMedianValue(List<Double> values) {

        if (values.size() > 0)
            return values.get(values.size()/2);
        return 0;
    }

    public long calculateWeeklyMedianValue(List<Date> dates) {

        Map<Long, Long> weeks = dates.stream()
                .collect(Collectors.groupingBy(x -> x.getTime() / (1000*60*60*24*7), Collectors.counting()));

        List<Long> values = new ArrayList<>(weeks.values());
        Collections.sort(values);

        if (values.size() > 0)
            return values.get(values.size()/2);
        return 0;
    }

    public long calculateDailyMedianValue(List<Date> dates) {

        Map<Long, Long> days = dates.stream()
                .collect(Collectors.groupingBy(x -> x.getTime() / (1000*60*60*24), Collectors.counting()));

        List<Long> values = new ArrayList<>(days.values());
        Collections.sort(values);

        if (values.size() > 0)
            return values.get(values.size()/2);
        return 0;
    }
}
