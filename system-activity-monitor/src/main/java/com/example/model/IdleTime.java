package com.example.model;

import java.time.LocalDateTime;

public class IdleTime {

    private Integer id;
    private User user;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;

    public IdleTime() {}

    public IdleTime(User user, LocalDateTime startTime) {
        this.user = user;
        this.startTime = startTime;
        this.durationSeconds = 0;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
}
