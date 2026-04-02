package com.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.risk.entity.RuleConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 预警规则配置 Mapper
 *
 * 继承 MyBatis-Plus BaseMapper，自动获得基础CRUD能力
 * deleted 字段的伪删除通过 @TableLogic 注解自动处理，
 * 查询时自动加 WHERE deleted=0，删除时自动执行 UPDATE SET deleted=1
 */
@Mapper
public interface RuleConfigMapper extends BaseMapper<RuleConfigEntity> {

    /**
     * 查询规则所有版本（含已删除）
     *
     * 直接写SQL绕过 @TableLogic 的自动过滤
     */
    @Select("SELECT * FROM rule_config WHERE rule_code = #{ruleCode} ORDER BY version DESC")
    List<RuleConfigEntity> selectVersionsIncludeDeleted(@Param("ruleCode") String ruleCode);
}
