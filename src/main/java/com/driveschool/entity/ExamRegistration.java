package com.driveschool.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("exam_registration")
public class ExamRegistration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long subjectId;
    private Long examLocationId;
    private LocalDate examDate;
    private String status;
    private java.math.BigDecimal score;
    private Integer isPassed;
    private Integer retryCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
