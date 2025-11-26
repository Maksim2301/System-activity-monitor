package com.example.repository.interfaces;

import com.example.model.IdleTime;
import java.time.LocalDateTime;
import java.util.List;

public interface IdleRepository {

    void save(IdleTime idleTime);
    List<IdleTime> findByUserId(Integer userId);
    List<IdleTime> findByUserIdAndStartTimeBetween(Integer userId, LocalDateTime start, LocalDateTime end);
    void deleteById(Integer id);
}
