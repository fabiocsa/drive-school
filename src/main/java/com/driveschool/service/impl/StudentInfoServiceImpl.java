package com.driveschool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.driveschool.config.CorsConfig;
import com.driveschool.entity.*;
import com.driveschool.mapper.*;
import com.driveschool.service.StudentInfoService;
import com.driveschool.util.PdfGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StudentInfoServiceImpl extends ServiceImpl<StudentInfoMapper, StudentInfo> implements StudentInfoService {

    private final UserMapper userMapper;
    private final VehicleTypeMapper vehicleTypeMapper;
    private final PdfGenerator pdfGenerator;
    private final LearningPhaseMapper learningPhaseMapper;
    private final CoachMapper coachMapper;

    public StudentInfoServiceImpl(UserMapper userMapper, VehicleTypeMapper vehicleTypeMapper,
                                   PdfGenerator pdfGenerator, LearningPhaseMapper learningPhaseMapper,
                                   CoachMapper coachMapper) {
        this.userMapper = userMapper;
        this.vehicleTypeMapper = vehicleTypeMapper;
        this.pdfGenerator = pdfGenerator;
        this.learningPhaseMapper = learningPhaseMapper;
        this.coachMapper = coachMapper;
    }

    @Override
    @Transactional
    public StudentInfo submitRegistration(Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        StudentInfo existing = getOne(new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, userId));
        if (existing != null) {
            throw new RuntimeException("您已提交过报名信息");
        }

        StudentInfo info = new StudentInfo();
        info.setUserId(userId);
        info.setIdCard((String) data.get("idCard"));
        info.setAddress((String) data.get("address"));
        info.setVehicleTypeId(Long.valueOf(data.get("vehicleTypeId").toString()));
        info.setIdCardFrontPhoto((String) data.get("idCardFrontPhoto"));
        info.setIdCardBackPhoto((String) data.get("idCardBackPhoto"));
        info.setHealthReportPhoto((String) data.get("healthReportPhoto"));
        info.setPhoto((String) data.get("photo"));
        info.setAuditStatus("PENDING");
        info.setMedicalStatus("PENDING");
        info.setAssignStatus("PENDING");
        info.setCertStatus("NONE");
        info.setRegistrationTime(java.time.LocalDateTime.now());
        save(info);

        autoAudit(info);
        return info;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }
        if (!CorsConfig.isAllowedExtension(file.getOriginalFilename())) {
            throw new RuntimeException("不支持的文件类型，仅支持 jpg、png、pdf");
        }
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String newFilename = UUID.randomUUID().toString() + ext;
        String filePath = CorsConfig.UPLOAD_PATH + newFilename;
        try {
            file.transferTo(new File(filePath));
            return newFilename;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 自动审核：仅校验车型存在性 + 年龄最低要求。
     * - 不通过 → REJECTED（写明原因）
     * - 通过 → 保持 PENDING（等待管理员人工审核）
     * 不再在此处检查体检状态，不再创建 LearningPhase 和 PDF。
     */
    @Override
    @Transactional
    public void autoAudit(StudentInfo info) {
        if (!"PENDING".equals(info.getAuditStatus())) return;

        VehicleType vt = vehicleTypeMapper.selectById(info.getVehicleTypeId());
        if (vt == null || vt.getEnabled() == 0) {
            info.setAuditStatus("REJECTED");
            info.setAuditRemark("报考车型不存在或已停用");
            updateById(info);
            return;
        }

        int age = calculateAge(info.getIdCard());
        if (vt.getMinAge() != null && age < vt.getMinAge()) {
            info.setAuditStatus("REJECTED");
            info.setAuditRemark("年龄不满足报考车型最低要求(需" + vt.getMinAge() + "岁)");
            updateById(info);
            return;
        }

        // 自动校验通过，保持 PENDING 等待管理员人工审核
        info.setAuditRemark("自动校验通过(车型/年龄)，等待管理员审核");
        updateById(info);
    }

    /**
     * 管理员手动审核。
     * - APPROVED: 必须体检 PASSED → 创建 LearningPhase + 生成 PDF → 记录审核日志
     * - REJECTED: 填写原因 → 记录审核日志
     */
    @Override
    @Transactional
    public void adminAudit(Long studentId, String status, String remark,
                           String medicalStatus, String auditedBy) {
        StudentInfo info = getById(studentId);
        if (info == null) throw new RuntimeException("学员信息不存在");

        // 仅 PENDING 和 REJECTED(重新审核) 状态允许审核
        if (!"PENDING".equals(info.getAuditStatus()) && !"REJECTED".equals(info.getAuditStatus())) {
            throw new RuntimeException("当前状态不可审核");
        }

        // 先更新体检状态（如果传入）
        if (medicalStatus != null && !medicalStatus.isEmpty()) {
            info.setMedicalStatus(medicalStatus);
        }

        if ("APPROVED".equals(status)) {
            if (!"PASSED".equals(info.getMedicalStatus())) {
                throw new RuntimeException("体检未合格，无法通过审核");
            }
            info.setAuditStatus("APPROVED");
            info.setAuditRemark(remark != null && !remark.isEmpty() ? remark : "管理员审核通过");
            info.setAuditedBy(auditedBy);
            info.setAuditedTime(java.time.LocalDateTime.now());
            updateById(info);

            // 审核通过后创建学习阶段并生成 PDF
            createLearningPhase(info.getId());
            try {
                User user = userMapper.selectById(info.getUserId());
                pdfGenerator.generateRegistrationForm(info, user);
                pdfGenerator.generateHealthReport(info, user);
                pdfGenerator.generateExamCard(info, user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("REJECTED".equals(status)) {
            info.setAuditStatus("REJECTED");
            info.setAuditRemark(remark != null && !remark.isEmpty() ? remark : "管理员审核不通过");
            info.setAuditedBy(auditedBy);
            info.setAuditedTime(java.time.LocalDateTime.now());
            updateById(info);
        }
    }

    @Override
    @Transactional
    public int batchAudit(java.util.List<Long> ids, String status, String remark,
                          String medicalStatus, String auditedBy) {
        int successCount = 0;
        for (Long id : ids) {
            try {
                adminAudit(id, status, remark, medicalStatus, auditedBy);
                successCount++;
            } catch (Exception e) {
                // 单个失败不影响其他，继续处理
            }
        }
        return successCount;
    }

    @Override
    public Map<String, Object> getMyInfo(Long userId) {
        StudentInfo info = getOne(new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, userId));
        if (info == null) return null;
        Map<String, Object> result = new HashMap<>();
        User user = userMapper.selectById(userId);
        result.put("studentInfo", info);
        result.put("user", user);
        if (info.getVehicleTypeId() != null) {
            result.put("vehicleType", vehicleTypeMapper.selectById(info.getVehicleTypeId()));
        }
        if (info.getCoachId() != null) {
            Coach coach = coachMapper.selectById(info.getCoachId());
            if (coach != null) {
                User coachUser = userMapper.selectById(coach.getUserId());
                Map<String, Object> coachMap = new HashMap<>();
                coachMap.put("coach", coach);
                coachMap.put("name", coachUser != null ? coachUser.getRealName() : "");
                coachMap.put("phone", coachUser != null ? coachUser.getPhone() : "");
                result.put("coachInfo", coachMap);
            }
        }
        LearningPhase phase = learningPhaseMapper.selectOne(
                new LambdaQueryWrapper<LearningPhase>().eq(LearningPhase::getStudentId, info.getId()));
        result.put("learningPhase", phase);
        return result;
    }

    @Override
    @Transactional
    public void assignCoach(Long studentId, Long coachId) {
        StudentInfo info = getById(studentId);
        if (info == null) throw new RuntimeException("学员不存在");
        if (!"APPROVED".equals(info.getAuditStatus())) {
            throw new RuntimeException("学员审核未通过，不可分配教练");
        }
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) throw new RuntimeException("教练不存在");

        info.setCoachId(coachId);
        info.setAssignStatus("ASSIGNED");
        updateById(info);
    }

    @Override
    public byte[] downloadPdf(Long studentId, String pdfType) {
        String pdfDir = CorsConfig.PDF_OUTPUT_PATH;
        String fileName;
        switch (pdfType) {
            case "registration":
                fileName = "registration_" + studentId + ".pdf";
                break;
            case "health":
                fileName = "health_" + studentId + ".pdf";
                break;
            case "examcard":
                fileName = "examcard_" + studentId + ".pdf";
                break;
            default:
                throw new RuntimeException("未知PDF类型");
        }
        try {
            File file = new File(pdfDir + fileName);
            byte[] bytes = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(bytes);
            }
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException("PDF文件读取失败");
        }
    }

    private int calculateAge(String idCard) {
        if (idCard == null || idCard.length() < 14) return 0;
        try {
            int year = Integer.parseInt(idCard.substring(6, 10));
            int month = Integer.parseInt(idCard.substring(10, 12));
            int day = Integer.parseInt(idCard.substring(12, 14));
            LocalDate birthDate = LocalDate.of(year, month, day);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Long getStudentInfoIdByUserId(Long userId) {
        StudentInfo info = getOne(new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, userId));
        return info != null ? info.getId() : null;
    }

    @Override
    public boolean isStudentOwnedByUser(Long studentInfoId, Long userId) {
        StudentInfo info = getById(studentInfoId);
        return info != null && info.getUserId().equals(userId);
    }

    private void createLearningPhase(Long studentId) {
        LearningPhase phase = new LearningPhase();
        phase.setStudentId(studentId);
        phase.setCurrentPhase("PHASE1");
        phase.setPhase1Hours(BigDecimal.ZERO);
        phase.setPhase2Hours(BigDecimal.ZERO);
        phase.setPhase3Hours(BigDecimal.ZERO);
        phase.setPhase4Hours(BigDecimal.ZERO);
        phase.setPhase1Completed(0);
        phase.setPhase2Completed(0);
        phase.setPhase3Completed(0);
        phase.setPhase4Completed(0);
        learningPhaseMapper.insert(phase);
    }
}
