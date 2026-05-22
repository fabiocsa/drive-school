package com.driveschool.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("coach")
public class Coach {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String gender;
    private Long vehicleTypeId;
    private Integer rating;
    private String scheduleJson;
}
