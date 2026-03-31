package com.tonghua.risk.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tonghua.risk.domain.entity.WarningRuleConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预警规则配置 Mapper
 *
 * 继承 MyBatis-Plus BaseMapper，自动获得基础CRUD能力
 * deleted 字段的伪删除通过 @TableLogic 注解自动处理，
 * 查询时自动加 WHERE deleted=0，删除时自动执行 UPDATE SET deleted=1
 */
@Mapper
public interface WarningRuleConfigMapper extends BaseMapper<WarningRuleConfig> {
}
