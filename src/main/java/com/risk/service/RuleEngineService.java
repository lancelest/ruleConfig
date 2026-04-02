package com.risk.service;

import com.risk.dto.BatchEvalRequest;
import com.risk.dto.RuleEvalRequest;
import com.risk.vo.BatchEvalResult;
import com.risk.vo.RuleEvalResult;

/**
 * 规则引擎服务接口
 * 
 * 核心功能：根据指标实际值，对照规则配置，判定是否触发预警及触发等级
 */
public interface RuleEngineService {

    /**
     * 单次评估：评估单个指标是否触发预警
     *
     * @param request 评估请求（组合代码、组合类型、指标编码、指标实际值）
     * @return 评估结果（是否触发、触发等级、规则编码、阈值等）
     */
    RuleEvalResult evaluate(RuleEvalRequest request);

    /**
     * 批量评估：数仓调用，批量评估多个组合的多个指标
     *
     * @param request 批量评估请求（评估日期 + 指标列表）
     * @return 批量评估结果（总数、触发数、各指标详情）
     */
    BatchEvalResult batchEvaluate(BatchEvalRequest request);
}