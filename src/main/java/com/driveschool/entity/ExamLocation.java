package com.driveschool.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("exam_location")
public class ExamLocation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String address;
    private Integer capacity;
    private String contact;
}
