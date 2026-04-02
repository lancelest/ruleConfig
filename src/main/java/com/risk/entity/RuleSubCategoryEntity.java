package com.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 规则细分类别配置实体
 * 
 * 用于支持动态扩展的细分类别配置，如固收+股票仓位按基准比例分档
 * 一条规则（ruleCode）下可能有多条细分类别配置
 */
@Data
@TableName("rule_sub_category")
public class RuleSubCategoryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则编码 */
    private String ruleCode;

    /** 细分类别编码（如 STOCK_RATIO_10） */
    private String subCategoryCode;

    /** 细分类别名称（如 基准股票比例10%） */
    private String subCategoryName;

    /** 层级1阈值 */
    private BigDecimal level1;

    /** 层级2阈值 */
    private BigDecimal level2;

    /** 层级3阈值 */
    private BigDecimal level3;

    /** 层级4阈值 */
    private BigDecimal level4;

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