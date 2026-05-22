package com.driveschool.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("vehicle_type")
public class VehicleType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer minAge;
    private String subjectHoursJson;
    private Integer enabled;
}
