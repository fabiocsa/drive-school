package com.driveschool.service;

import java.util.Map;

public interface StatisticsService {
    Map<String, Object> getRegistrationStats(int year, Integer month);
    Map<String, Object> getPassRateStats();
    Map<String, Object> getCoachWorkloadStats();
}
