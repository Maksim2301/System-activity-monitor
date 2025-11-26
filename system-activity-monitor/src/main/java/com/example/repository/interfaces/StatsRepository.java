package com.example.repository.interfaces;

import com.example.model.SystemStats;
import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository {

    void save(SystemStats systemStats);
    List<SystemStats> findByUserIdAndRecordedAtBetween(Integer userId, LocalDateTime start, LocalDateTime end);
    void deleteById(Integer id);
}
