package com.driveschool.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("student_info")
public class StudentInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String idCard;
    private String address;
    private Long vehicleTypeId;
    private String idCardFrontPhoto;
    private String idCardBackPhoto;
    private String healthReportPhoto;
    private String photo;
    private String auditStatus;
    private String auditRemark;
    private String medicalStatus;
    private Long coachId;
    private String assignStatus;
    private String certStatus;
    /** 审核人用户名 */
    private String auditedBy;
    /** 审核时间 */
    private LocalDateTime auditedTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime registrationTime;
}
