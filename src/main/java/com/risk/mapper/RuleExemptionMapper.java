package com.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.risk.entity.RuleExemptionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 豁免组合配置 Mapper
 */
@Mapper
public interface RuleExemptionMapper extends BaseMapper<RuleExemptionEntity> {
}