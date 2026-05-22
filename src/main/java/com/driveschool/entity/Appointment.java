package com.driveschool.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long coachId;
    private String appointmentTime;
    private LocalDate appointmentDate;
    private String status;
    private String cancelReason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
