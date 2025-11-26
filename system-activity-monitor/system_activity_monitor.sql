CREATE DATABASE IF NOT EXISTS system_activity_monitor CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE system_activity_monitor;

CREATE DATABASE IF NOT EXISTS system_activity_monitor
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE system_activity_monitor;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at DATETIME NOT NULL
    );

CREATE TABLE IF NOT EXISTS idle_time (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    duration_seconds INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS system_stats (
                                            id INT AUTO_INCREMENT PRIMARY KEY,
                                            user_id INT NOT NULL,
                                            cpu_load DECIMAL(6,2),
                                            ram_used_mb DECIMAL(12,2),
                                            ram_total_mb DECIMAL(12,2),
                                            active_window VARCHAR(255),
                                            keyboard_presses INT DEFAULT 0,
                                            mouse_clicks INT DEFAULT 0,
                                            mouse_moves BIGINT DEFAULT 0,
                                            system_uptime_seconds BIGINT,
                                            disk_total_gb DECIMAL(12,2),
                                            disk_free_gb DECIMAL(12,2),
                                            disk_used_gb DECIMAL(12,2),
                                            recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE reports (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         user_id INT NOT NULL,
                         report_name VARCHAR(255),
                         period_start DATE,
                         period_end DATE,
                         cpu_avg DECIMAL(10,2),
                         ram_avg DECIMAL(10,2),
                         idle_time_total_seconds DECIMAL(10,2),
                         avg_uptime_hours DECIMAL(10,2),
                         app_usage_json TEXT,
                         file_path TEXT,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         days_json LONGTEXT,
                         FOREIGN KEY (user_id) REFERENCES users(id)
);


