package com.driveschool.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.driveschool.entity.Coach;

import java.util.List;
import java.util.Map;

public interface CoachService extends IService<Coach> {
    List<Map<String, Object>> recommendCoaches(Long vehicleTypeId);
    List<Map<String, Object>> listWithDetails();
    Map<String, Object> getMyCoachInfo(Long userId);
}
