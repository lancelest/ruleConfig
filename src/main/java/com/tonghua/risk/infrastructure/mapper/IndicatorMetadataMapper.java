package com.tonghua.risk.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tonghua.risk.domain.entity.IndicatorMetadata;
import org.apache.ibatis.annotations.Mapper;

/**
 * 指标元数据 Mapper
 */
@Mapper
public interface IndicatorMetadataMapper extends BaseMapper<IndicatorMetadata> {
}
