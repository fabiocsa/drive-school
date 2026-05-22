package com.driveschool.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("fee_standard")
public class FeeStandard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String itemName;
    private String feeType;
    private java.math.BigDecimal amount;
}
