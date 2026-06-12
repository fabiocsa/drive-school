package com.driveschool.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.driveschool.entity.StudentInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface StudentInfoService extends IService<StudentInfo> {
    StudentInfo submitRegistration(Map<String, Object> data);
    String uploadFile(MultipartFile file);
    void autoAudit(StudentInfo info);
    /**
     * 管理员手动审核单个学员
     * @param studentId 学员信息ID
     * @param status 审核结果 APPROVED / REJECTED
     * @param remark 审核备注
     * @param medicalStatus 体检状态 (PASSED / FAILED)，可为 null 表示不修改
     * @param auditedBy 审核人用户名
     */
    void adminAudit(Long studentId, String status, String remark, String medicalStatus, String auditedBy);

    /**
     * 批量审核学员
     * @param ids 学员信息ID列表
     * @param status 审核结果
     * @param remark 审核备注
     * @param medicalStatus 体检状态
     * @param auditedBy 审核人用户名
     * @return 成功审核的数量
     */
    int batchAudit(java.util.List<Long> ids, String status, String remark, String medicalStatus, String auditedBy);
    Map<String, Object> getMyInfo(Long userId);
    void assignCoach(Long studentId, Long coachId);
    byte[] downloadPdf(Long studentId, String pdfType);

    /**
     * 根据 userId 获取对应的 studentInfoId
     */
    Long getStudentInfoIdByUserId(Long userId);

    /**
     * 验证 studentInfo 记录是否属于指定用户
     */
    boolean isStudentOwnedByUser(Long studentInfoId, Long userId);
}
