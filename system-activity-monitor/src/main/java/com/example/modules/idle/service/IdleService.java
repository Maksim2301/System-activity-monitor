package com.example.modules.idle.service;

import com.example.model.IdleTime;
import com.example.model.User;
import com.example.repository.impl.IdleRepositoryImpl;
import com.example.repository.interfaces.IdleRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class IdleService {

    private final IdleRepository idleRepository = new IdleRepositoryImpl();

    public IdleTime startIdle(User user) {
        validateUser(user);

        if (isIdleActive(user)) {
            return getActiveIdle(user).get();
        }

        IdleTime idle = new IdleTime(user, LocalDateTime.now());
        user.getIdleTimes().add(idle);

        idleRepository.save(idle);

        System.out.println("OFFLINE mode is enabled. Downtime started at " + idle.getStartTime());
        return idle;
    }

    public IdleTime endIdle(User user) {
        validateUser(user);

        Optional<IdleTime> activeIdleOpt = getActiveIdle(user);

        if (activeIdleOpt.isEmpty()) {
            System.out.println("No active downtime. ONLINE is already enabled.");
            return null;
        }

        IdleTime activeIdle = activeIdleOpt.get();

        LocalDateTime end = LocalDateTime.now();
        long durationSec = Duration.between(activeIdle.getStartTime(), end).getSeconds();

        if (durationSec < 0) durationSec = 0;

        activeIdle.setEndTime(end);
        activeIdle.setDurationSeconds((int) durationSec);

        idleRepository.save(activeIdle);

        System.out.println("ONLINE mode is enabled. Downtime is complete. (" + durationSec + " sec).");

        return activeIdle;
    }

    public boolean isIdleActive(User user) {
        return getActiveIdle(user).isPresent();
    }

    public Optional<IdleTime> getActiveIdle(User user) {
        validateUser(user);
        return user.getIdleTimes().stream()
                .filter(i -> i.getEndTime() == null)
                .findFirst();
    }

    private void validateUser(User user) {
        if (user == null || user.getId() == null)
            throw new IllegalArgumentException("Log in");
    }
}
