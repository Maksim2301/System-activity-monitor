package com.example.repository.impl;

import com.example.model.SystemStats;
import com.example.repository.interfaces.StatsRepository;
import com.example.connection.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StatsRepositoryImpl implements StatsRepository {

    @Override
    public void save(SystemStats s) {
        String sql = """
                INSERT INTO system_stats (
                    user_id, cpu_load,
                    ram_used_mb, ram_total_mb,
                    active_window,
                    keyboard_presses, mouse_clicks, mouse_moves,
                    system_uptime_seconds,
                    disk_total_gb, disk_free_gb, disk_used_gb,
                    recorded_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getUser().getId());
            ps.setBigDecimal(2, s.getCpuLoad());

            ps.setBigDecimal(3, s.getRamUsedMb());
            ps.setBigDecimal(4, s.getRamTotalMb());

            ps.setString(5, s.getActiveWindow());
            ps.setInt(6, s.getKeyboardPresses());
            ps.setInt(7, s.getMouseClicks());
            ps.setLong(8, s.getMouseMoves() != null ? s.getMouseMoves() : 0);

            if (s.getSystemUptimeSeconds() != null)
                ps.setLong(9, s.getSystemUptimeSeconds());
            else
                ps.setNull(9, Types.BIGINT);

            ps.setBigDecimal(10, s.getDiskTotalGb());
            ps.setBigDecimal(11, s.getDiskFreeGb());
            ps.setBigDecimal(12, s.getDiskUsedGb());

            if (s.getRecordedAt() == null)
                s.setRecordedAt(LocalDateTime.now());

            ps.setTimestamp(13, Timestamp.valueOf(s.getRecordedAt()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка при збереженні SystemStats: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SystemStats> findByUserIdAndRecordedAtBetween(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql = """
                SELECT * FROM system_stats
                WHERE user_id = ? AND recorded_at BETWEEN ? AND ?
                ORDER BY recorded_at ASC
                """;

        List<SystemStats> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка пошуку SystemStats: " + e.getMessage(), e);
        }

        return list;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM system_stats WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Помилка видалення SystemStats: " + e.getMessage(), e);
        }
    }

    private SystemStats mapRow(ResultSet rs) throws SQLException {
        SystemStats s = new SystemStats();

        s.setId(rs.getInt("id"));
        s.setCpuLoad(rs.getBigDecimal("cpu_load"));

        s.setRamUsedMb(rs.getBigDecimal("ram_used_mb"));
        s.setRamTotalMb(rs.getBigDecimal("ram_total_mb"));

        s.setActiveWindow(rs.getString("active_window"));
        s.setKeyboardPresses(rs.getInt("keyboard_presses"));
        s.setMouseClicks(rs.getInt("mouse_clicks"));
        s.setMouseMoves(rs.getLong("mouse_moves"));

        long uptime = rs.getLong("system_uptime_seconds");
        if (!rs.wasNull()) s.setSystemUptimeSeconds(uptime);

        s.setDiskTotalGb(rs.getBigDecimal("disk_total_gb"));
        s.setDiskFreeGb(rs.getBigDecimal("disk_free_gb"));
        s.setDiskUsedGb(rs.getBigDecimal("disk_used_gb"));

        Timestamp ts = rs.getTimestamp("recorded_at");
        if (ts != null) s.setRecordedAt(ts.toLocalDateTime());

        return s;
    }
}
