package com.example.repository.impl;

import com.example.model.User;
import com.example.repository.interfaces.UserRepository;
import com.example.connection.DatabaseConnection;

import java.sql.*;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    @Override
    public void save(User user) {
        boolean isNew = (user.getId() == null);

        String sqlInsert = """
                INSERT INTO users (username, password_hash, email, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """;

        String sqlUpdate = """
                UPDATE users
                SET username = ?, password_hash = ?, email = ?
                WHERE id = ?
                """;

        String sql = isNew ? sqlInsert : sqlUpdate;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());

            if (!isNew) {
                ps.setInt(4, user.getId());
            }

            ps.executeUpdate();

            if (isNew) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) user.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося зберегти користувача: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return Optional.of(mapUser(rs));
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Помилка пошуку користувача: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return Optional.of(mapUser(rs));
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Помилка пошуку користувача по ID: " + e.getMessage(), e);
        }
    }

    @Override
    public void updatePassword(Integer userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося оновити пароль: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        if (id == null) return;

        String sql = "DELETE FROM users WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося видалити користувача: " + e.getMessage(), e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setEmail(rs.getString("email"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());

        return u;
    }
}
