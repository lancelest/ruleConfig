package com.tonghua.risk.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 指标元数据实体
 */
@Data
@TableName("indicator_metadata")
public class IndicatorMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 指标编码，如：excess_return_1y */
    private String indicatorCode;

    /** 指标名称，如：超额收益_近一年 */
    private String indicatorName;

    /** 指标分类编码，如：performance */
    private String categoryCode;

    /** 指标分类名称，如：业绩表现 */
    private String categoryName;

    /** 指标计算逻辑说明 */
    private String calcFormula;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
