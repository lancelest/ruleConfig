package com.risk.dto;

import lombok.Data;

import java.util.List;

/**
 * 规则引擎批量评估请求参数
 * 
 * 用于数仓调用，批量评估多个组合的多个指标
 */
@Data
public class BatchEvalRequest {

    /** 评估日期 */
    private String evalDate;

    /** 待评估的指标列表 */
    private List<MetricItem> metrics;

    @Data
    public static class MetricItem {
        /** 组合代码 */
        private String portCode;

        /** 组合类型编码 */
        private String portTypeCode;

        /** 指标编码 */
        private String metricCode;

        /** 指标实际值 */
        private java.math.BigDecimal metricValue;

        /** 细分类别编码（可选） */
        private String subCategoryCode;
    }
}