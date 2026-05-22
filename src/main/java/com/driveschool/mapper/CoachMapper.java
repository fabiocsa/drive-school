package com.driveschool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.driveschool.entity.Coach;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoachMapper extends BaseMapper<Coach> {
}
