package com.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 豁免组合配置实体
 * 
 * 用于配置某些组合类型对特定规则/层级的豁免
 * 豁免级别：组合类型级别，非具体组合代码
 */
@Data
@TableName("rule_exemption")
public class RuleExemptionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则编码 */
    private String ruleCode;

    /** 豁免层级 1/2/3/4 */
    private Integer level;

    /** 组合类型编码 */
    private String portTypeCode;

    /** 组合类型名称 */
    private String portTypeName;

    /** 开关 0-关闭 1-开启 */
    private Integer isEnabled;

    /** 豁免原因 */
    private String reason;

    /** 生效时间 */
    private LocalDateTime effectiveTime;

    /** 失效时间 */
    private LocalDateTime expireTime;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}