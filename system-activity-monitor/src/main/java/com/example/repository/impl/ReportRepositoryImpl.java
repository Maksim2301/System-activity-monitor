package com.example.repository.impl;

import com.example.model.DaySummary;
import com.example.model.Report;
import com.example.repository.interfaces.ReportRepository;
import com.example.connection.DatabaseConnection;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;

public class ReportRepositoryImpl implements ReportRepository {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, type, context) ->
                    new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, context) ->
                    LocalDate.parse(json.getAsString()))
            .create();

    private static final Type MAP_TYPE =
            new com.google.gson.reflect.TypeToken<Map<String, BigDecimal>>() {}.getType();

    private static final Type DAYS_LIST_TYPE =
            new com.google.gson.reflect.TypeToken<List<DaySummary>>() {}.getType();

    @Override
    public void save(Report report) {
        String sql = """
            INSERT INTO reports 
            (user_id, report_name, period_start, period_end, cpu_avg, ram_avg,
             idle_time_total_seconds, avg_uptime_hours,
             app_usage_json, file_path, days_json)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, report.getUser().getId());
            ps.setString(2, report.getReportName());
            ps.setDate(3, Date.valueOf(report.getPeriodStart()));
            ps.setDate(4, Date.valueOf(report.getPeriodEnd()));
            ps.setBigDecimal(5, report.getCpuAvg());
            ps.setBigDecimal(6, report.getRamAvg());
            ps.setBigDecimal(7, report.getIdleTimeTotalSeconds());
            ps.setBigDecimal(8, report.getAvgUptimeHours());

            ps.setString(9, gson.toJson(
                    report.getAppUsagePercent() != null ? report.getAppUsagePercent() : Map.of(),
                    MAP_TYPE
            ));

            ps.setString(10, report.getFilePath());
            ps.setString(11, gson.toJson(
                    report.getDays() != null ? report.getDays() : List.of(),
                    DAYS_LIST_TYPE
            ));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) report.setId(keys.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка при створенні звіту: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Report report) {
        String sql = """
            UPDATE reports SET
                report_name = ?, period_start = ?, period_end = ?, cpu_avg = ?, ram_avg = ?,
                idle_time_total_seconds = ?, avg_uptime_hours = ?, 
                app_usage_json = ?, file_path = ?, days_json = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, report.getReportName());
            ps.setDate(2, Date.valueOf(report.getPeriodStart()));
            ps.setDate(3, Date.valueOf(report.getPeriodEnd()));
            ps.setBigDecimal(4, report.getCpuAvg());
            ps.setBigDecimal(5, report.getRamAvg());
            ps.setBigDecimal(6, report.getIdleTimeTotalSeconds());
            ps.setBigDecimal(7, report.getAvgUptimeHours());

            ps.setString(8, gson.toJson(
                    report.getAppUsagePercent() != null ? report.getAppUsagePercent() : Map.of(),
                    MAP_TYPE
            ));

            ps.setString(9, report.getFilePath());
            ps.setString(10, gson.toJson(
                    report.getDays() != null ? report.getDays() : List.of(),
                    DAYS_LIST_TYPE
            ));

            ps.setInt(11, report.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Помилка при оновленні звіту: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Report> findByUserIdAndPeriodBetween(Integer userId, LocalDate start, LocalDate end) {
        String sql = """
        SELECT * FROM reports 
        WHERE user_id = ?
          AND period_start >= ?
          AND period_end <= ?
        ORDER BY period_start
        """;

        return executeQuery(sql, ps -> {
            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));
        });
    }

    @Override
    public Optional<Report> findById(Integer id) {
        List<Report> list = executeQuery(
                "SELECT * FROM reports WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public void deleteById(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM reports WHERE id = ?")) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Помилка при видаленні звіту: " + e.getMessage(), e);
        }
    }
    @Override
    public List<Report> findByUserId(Integer userId) {
        String sql = "SELECT * FROM reports WHERE user_id = ? ORDER BY created_at DESC";
        return executeQuery(sql, ps -> ps.setInt(1, userId));
    }

    private List<Report> executeQuery(String sql, SQLConsumer<PreparedStatement> paramsSetter) {
        List<Report> reports = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            paramsSetter.accept(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapReport(rs));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Помилка SELECT у reports: " + e.getMessage(), e);
        }

        return reports;
    }

    private Report mapReport(ResultSet rs) throws SQLException {
        Report r = new Report();

        r.setId(rs.getInt("id"));
        r.setReportName(rs.getString("report_name"));
        r.setPeriodStart(rs.getDate("period_start").toLocalDate());
        r.setPeriodEnd(rs.getDate("period_end").toLocalDate());
        r.setCpuAvg(rs.getBigDecimal("cpu_avg"));
        r.setRamAvg(rs.getBigDecimal("ram_avg"));
        r.setIdleTimeTotalSeconds(rs.getBigDecimal("idle_time_total_seconds"));
        r.setAvgUptimeHours(rs.getBigDecimal("avg_uptime_hours"));
        r.setFilePath(rs.getString("file_path"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());

        String jsonApps = rs.getString("app_usage_json");
        r.setAppUsagePercent(jsonApps != null && !jsonApps.isBlank()
                ? gson.fromJson(jsonApps, MAP_TYPE)
                : new LinkedHashMap<>());

        String daysJson = rs.getString("days_json");
        r.setDays(daysJson != null && !daysJson.isBlank()
                ? gson.fromJson(daysJson, DAYS_LIST_TYPE)
                : new ArrayList<>());

        return r;
    }

    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T t) throws Exception;
    }
}
