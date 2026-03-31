package com.tonghua.risk.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警规则配置实体
 *
 * 设计要点：
 * 1. 组合维度和指标维度共用一张表，通过查询视角区分
 * 2. version + status 实现版本管理
 * 3. deleted 字段实现伪删除
 */
@Data
@TableName("warning_rule_config")
public class WarningRuleConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则编码，业务唯一标识 */
    private String ruleCode;

    /** 规则名称 */
    private String ruleName;

    /** 组合类型编码（如：active_equity） */
    private String portfolioTypeCode;

    /** 组合类型名称（如：主动权益） */
    private String portfolioTypeName;

    /** 指标编码 */
    private String indicatorCode;

    /** 指标名称 */
    private String indicatorName;

    /** 细分类别（如：基准股票比例10%~20%） */
    private String subCategory;

    /**
     * 规则条件定义（JSON格式）
     * 存储多指标AND组合条件，结构见 RuleConditions
     */
    private String ruleConditions;

    /** 层级1阈值（正常边界） */
    private BigDecimal thresholdL1;

    /** 层级2阈值（警告） */
    private BigDecimal thresholdL2;

    /** 层级3阈值（错误） */
    private BigDecimal thresholdL3;

    /** 层级4阈值（严重） */
    private BigDecimal thresholdL4;

    /** 版本号，每次保存+1 */
    private Integer version;

    /**
     * 状态
     * DRAFT-草稿, ACTIVE-生效, INACTIVE-停用, ARCHIVED-归档
     */
    private String status;

    /** 是否当前生效版本：0-否，1-是 */
    private Integer isCurrentVersion;

    /** 生效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTime;

    /** 失效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 是否删除：0-未删除，1-已删除（伪删除） */
    @TableLogic
    private Integer deleted;
}
