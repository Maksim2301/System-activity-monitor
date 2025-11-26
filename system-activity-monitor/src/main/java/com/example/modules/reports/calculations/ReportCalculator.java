package com.example.modules.reports.calculations;

import com.example.model.DaySummary;
import com.example.model.HourStat;
import com.example.model.IdleTime;
import com.example.model.SystemStats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReportCalculator {

    public BigDecimal average(List<SystemStats> stats, Function<SystemStats, BigDecimal> mapper) {
        if (stats == null || stats.isEmpty()) return BigDecimal.ZERO;

        List<BigDecimal> values = stats.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .toList();

        if (values.isEmpty()) return BigDecimal.ZERO;

        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal totalIdle(List<IdleTime> idleList) {
        if (idleList == null || idleList.isEmpty()) return BigDecimal.ZERO;

        long sum = idleList.stream()
                .map(IdleTime::getDurationSeconds)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();

        return BigDecimal.valueOf(sum);
    }

    public Map<String, BigDecimal> appUsagePercent(List<SystemStats> stats) {
        if (stats == null || stats.isEmpty()) return Map.of();

        long total = stats.size();

        Map<String, Long> counts = stats.stream()
                .map(SystemStats::getActiveWindow)
                .filter(Objects::nonNull)
                .map(this::normalizeAppName)
                .collect(Collectors.groupingBy(s -> s, LinkedHashMap::new, Collectors.counting()));

        Map<String, BigDecimal> result = new LinkedHashMap<>();
        counts.forEach((name, count) -> {
            BigDecimal percent = BigDecimal.valueOf(count * 100.0 / total)
                    .setScale(2, RoundingMode.HALF_UP);
            result.put(name, percent);
        });

        return result;
    }

    public Map<LocalDate, BigDecimal> dailyUptime(List<SystemStats> stats) {

        if (stats == null || stats.isEmpty())
            return Map.of();

        Map<LocalDate, List<SystemStats>> grouped =
                stats.stream()
                        .filter(s -> s.getRecordedAt() != null)
                        .collect(Collectors.groupingBy(
                                s -> s.getRecordedAt().toLocalDate(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();

        for (var entry : grouped.entrySet()) {

            List<SystemStats> list = entry.getValue();

            if (list.size() < 2) {
                result.put(entry.getKey(), BigDecimal.ZERO);
                continue;
            }

            long totalDiff = 0;
            int count = 0;

            for (int i = 1; i < list.size(); i++) {
                long diff = Duration.between(
                        list.get(i - 1).getRecordedAt(),
                        list.get(i).getRecordedAt()
                ).getSeconds();

                if (diff > 0 && diff <= 300) {
                    totalDiff += diff;
                    count++;
                }
            }

            long avgInterval = count > 0 ? totalDiff / count : 10;
            long uptimeSeconds = avgInterval * list.size();

            BigDecimal hours = BigDecimal.valueOf(uptimeSeconds / 3600.0)
                    .setScale(2, RoundingMode.HALF_UP);

            result.put(entry.getKey(), hours);
        }

        return result;
    }

    public List<DaySummary> buildDaySummary(List<SystemStats> stats) {
        if (stats == null || stats.isEmpty()) return List.of();

        Map<LocalDate, Map<Integer, List<SystemStats>>> grouped =
                stats.stream()
                        .filter(s -> s.getRecordedAt() != null)
                        .collect(Collectors.groupingBy(
                                s -> s.getRecordedAt().toLocalDate(),
                                Collectors.groupingBy(s -> s.getRecordedAt().getHour())
                        ));

        Map<LocalDate, BigDecimal> uptimeByDay = dailyUptime(stats);

        List<DaySummary> days = new ArrayList<>();

        for (var dayEntry : grouped.entrySet()) {

            LocalDate date = dayEntry.getKey();
            Map<Integer, List<SystemStats>> hoursMap = dayEntry.getValue();

            List<HourStat> hourStats = hoursMap.entrySet().stream()
                    .map(h -> new HourStat(
                            h.getKey(),
                            average(h.getValue(), SystemStats::getCpuLoad),
                            average(h.getValue(), SystemStats::getRamUsedMb)
                    ))
                    .sorted(Comparator.comparing(HourStat::getHour))
                    .toList();

            List<SystemStats> dayStats = hoursMap.values().stream()
                    .flatMap(List::stream)
                    .toList();

            BigDecimal dailyCpu = average(dayStats, SystemStats::getCpuLoad);
            BigDecimal dailyRam = average(dayStats, SystemStats::getRamUsedMb);
            Map<String, BigDecimal> dailyApps = appUsagePercent(dayStats);

            DaySummary daySummary = new DaySummary(
                    date,
                    hourStats,
                    uptimeByDay.getOrDefault(date, BigDecimal.ZERO)
            );

            daySummary.setCpuAvg(dailyCpu);
            daySummary.setRamAvg(dailyRam);
            daySummary.setAppUsagePercentByDay(dailyApps);
            for (HourStat h : hourStats) {
                h.setParentDay(daySummary);
            }

            days.add(daySummary);
        }

        days.sort(Comparator.comparing(DaySummary::getDate));
        return days;
    }

    public BigDecimal averageUptime(List<SystemStats> stats) {

        if (stats == null || stats.isEmpty())
            return BigDecimal.ZERO;
        Map<LocalDate, BigDecimal> daily = dailyUptime(stats);

        if (daily.isEmpty())
            return BigDecimal.ZERO;

        BigDecimal sum = daily.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(
                BigDecimal.valueOf(daily.size()),
                2,
                RoundingMode.HALF_UP
        );
    }

    private String normalizeAppName(String t) {
        if (t == null) return "Робочий стіл";

        String s = t.toLowerCase();

        if (s.contains("chrome")) return "Google Chrome";
        if (s.contains("firefox")) return "Mozilla Firefox";
        if (s.contains("edge")) return "Microsoft Edge";
        if (s.contains("opera")) return "Opera Browser";
        if (s.contains("word")) return "MS Word";
        if (s.contains("excel")) return "MS Excel";
        if (s.contains("idea")) return "IntelliJ IDEA";
        if (s.contains("studio")) return "Android Studio";
        if (s.contains("telegram")) return "Telegram";
        if (s.contains("viber")) return "Viber";

        return t.length() > 40 ? t.substring(0, 40) + "..." : t;
    }
}
