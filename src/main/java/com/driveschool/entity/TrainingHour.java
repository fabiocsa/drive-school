package com.driveschool.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("training_hour")
public class TrainingHour {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long coachId;
    private java.math.BigDecimal duration;
    private String content;
    private LocalDate recordDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
