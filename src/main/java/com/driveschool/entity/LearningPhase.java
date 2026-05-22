package com.driveschool.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("learning_phase")
public class LearningPhase {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String currentPhase;
    private java.math.BigDecimal phase1Hours;
    private java.math.BigDecimal phase2Hours;
    private java.math.BigDecimal phase3Hours;
    private java.math.BigDecimal phase4Hours;
    private Integer phase1Completed;
    private Integer phase2Completed;
    private Integer phase3Completed;
    private Integer phase4Completed;
}
