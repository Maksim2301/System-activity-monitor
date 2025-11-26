package com.example.repository.factory;

import com.example.repository.impl.*;
import com.example.repository.interfaces.*;
import com.example.repository.impl.IdleRepositoryImpl;
import com.example.repository.impl.ReportRepositoryImpl;
import com.example.repository.impl.StatsRepositoryImpl;
import com.example.repository.impl.UserRepositoryImpl;
import com.example.repository.interfaces.IdleRepository;
import com.example.repository.interfaces.ReportRepository;
import com.example.repository.interfaces.StatsRepository;
import com.example.repository.interfaces.UserRepository;

public class RepositoryFactory {

    private static final ReportRepository REPORT_REPOSITORY = new ReportRepositoryImpl();
    private static final StatsRepository STATS_REPOSITORY = new StatsRepositoryImpl();
    private static final IdleRepository IDLE_REPOSITORY = new IdleRepositoryImpl();
    private static final UserRepository USER_REPOSITORY = new UserRepositoryImpl();

    public static ReportRepository getReportRepository() {
        return REPORT_REPOSITORY;
    }

    public static StatsRepository getStatsRepository() {
        return STATS_REPOSITORY;
    }

    public static IdleRepository getIdleRepository() {
        return IDLE_REPOSITORY;
    }

    public static UserRepository getUserRepository() {
        return USER_REPOSITORY;
    }
}
