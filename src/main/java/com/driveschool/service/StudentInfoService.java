package com.driveschool.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.driveschool.entity.StudentInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface StudentInfoService extends IService<StudentInfo> {
    StudentInfo submitRegistration(Map<String, Object> data);
    String uploadFile(MultipartFile file);
    void autoAudit(StudentInfo info);
    void adminAudit(Long studentId, String status, String remark);
    Map<String, Object> getMyInfo(Long userId);
    void assignCoach(Long studentId, Long coachId);
    byte[] downloadPdf(Long studentId, String pdfType);
}
