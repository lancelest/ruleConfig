package com.risk.controller;

import com.risk.dto.BatchEvalRequest;
import com.risk.dto.RuleEvalRequest;
import com.risk.dto.ResultVO;
import com.risk.service.RuleEngineService;
import com.risk.vo.BatchEvalResult;
import com.risk.vo.RuleEvalResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 规则引擎控制器
 * 
 * 提供规则评估接口，供数仓调用或前端调试使用
 */
@RestController
@RequestMapping("/api/rule-engine")
public class RuleEngineController {

    @Resource
    private RuleEngineService ruleEngineService;

    /**
     * 单次评估
     * 
     * 评估单个指标是否触发预警
     * 
     * @param request 评估请求（组合代码、组合类型、指标编码、指标实际值）
     * @return 评估结果（是否触发、触发等级、规则编码、阈值等）
     */
    @PostMapping("/evaluate")
    public ResultVO<RuleEvalResult> evaluate(@RequestBody RuleEvalRequest request) {
        RuleEvalResult result = ruleEngineService.evaluate(request);
        return ResultVO.success(result);
    }

    /**
     * 批量评估
     * 
     * 数仓调用，批量评估多个组合的多个指标
     * 
     * @param request 批量评估请求（评估日期 + 指标列表）
     * @return 批量评估结果（总数、触发数、各指标详情）
     */
    @PostMapping("/batch")
    public ResultVO<BatchEvalResult> batchEvaluate(@RequestBody BatchEvalRequest request) {
        BatchEvalResult result = ruleEngineService.batchEvaluate(request);
        return ResultVO.success(result);
    }
}