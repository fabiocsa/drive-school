package com.driveschool.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.driveschool.entity.Coach;

import java.util.List;
import java.util.Map;

public interface CoachService extends IService<Coach> {
    List<Map<String, Object>> recommendCoaches(Long vehicleTypeId);
    List<Map<String, Object>> listWithDetails();
    Map<String, Object> getMyCoachInfo(Long userId);

    /**
     * 根据 userId 获取教练信息
     */
    Coach getCoachByUserId(Long userId);

    /**
     * 验证指定学员是否属于指定教练
     */
    boolean isStudentAssignedToCoach(Long studentInfoId, Long coachId);

    /**
     * 更新教练空闲档期（含容量设置）
     * @param userId       教练的 userId
     * @param scheduleJson 新的档期 JSON 字符串
     *                     格式: [{"dayTime":"周一上午","capacity":5},...]
     *                     兼容旧格式: ["周一上午","周一下午"]
     * @return 更新后的教练信息
     */
    Coach updateSchedule(Long userId, String scheduleJson);

    /**
     * 模糊搜索当前教练名下的学员
     * @param coachUserId 教练的 userId
     * @param keyword     搜索关键词（匹配姓名或学号ID）
     * @return 学员列表，含基本信息 + 学时汇总
     */
    List<Map<String, Object>> searchStudents(Long coachUserId, String keyword);

    /**
     * 获取学员详细信息摘要（供选中后展示）
     * @param coachUserId  教练的 userId
     * @param studentInfoId 学员信息 ID
     * @return 学员详细信息（基本信息 + 阶段 + 学时汇总）
     */
    Map<String, Object> getStudentSummary(Long coachUserId, Long studentInfoId);
}
