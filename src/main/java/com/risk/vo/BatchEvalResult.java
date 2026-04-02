package com.risk.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量评估结果
 * 
 * 用于返回批量评估的汇总结果
 */
@Data
public class BatchEvalResult {

    /** 总评估数量 */
    private Integer total;

    /** 触发预警的数量 */
    private Integer triggered;

    /** 各指标评估结果详情 */
    private List<EvalDetail> results;

    @Data
    public static class EvalDetail {
        /** 组合代码 */
        private String portCode;

        /** 指标编码 */
        private String metricCode;

        /** 是否触发 */
        private Boolean triggered;

        /** 触发等级 */
        private Integer level;

        /** 规则编码 */
        private String ruleCode;

        /** 指标实际值 */
        private BigDecimal actualValue;

        /** 触发阈值 */
        private BigDecimal thresholdValue;

        /** 结果说明 */
        private String message;
    }
}