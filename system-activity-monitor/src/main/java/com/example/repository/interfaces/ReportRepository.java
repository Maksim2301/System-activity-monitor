package com.example.repository.interfaces;

import com.example.model.Report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReportRepository {

    void save(Report report);
    List<Report> findByUserIdAndPeriodBetween(Integer userId, LocalDate start, LocalDate end);
    Optional<Report> findById(Integer id);
    List<Report> findByUserId(Integer userId);
    void update(Report report);
    void deleteById(Integer id);
}
