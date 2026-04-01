package com.risk.controller;

import com.risk.dto.ResultVO;
import com.risk.dto.RuleConfigDTO;
import com.risk.entity.WarningRuleConfigEntity;
import com.risk.service.RuleConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预警规则配置 Controller
 *
 * 对应原型 Page4/Page5：预警规则配置页面
 * - 组合维度：按组合类型查看和管理规则
 * - 指标维度：按指标查看和管理规则
 *
 * 伪删除说明：调用删除接口后，数据不会物理删除，
 * 而是标记 deleted=1，后续查询自动过滤已删除数据
 */
@RestController
@RequestMapping("/api/rule-config")
public class RuleConfigController {

    @Autowired
    private RuleConfigService ruleConfigService;

    // ==================== 新增 ====================

    /**
     * 新增规则
     *
     * 生成 version=1，status=DRAFT 的初始版本
     */
    @PostMapping
    public ResultVO<WarningRuleConfigEntity> create(@Validated @RequestBody RuleConfigDTO dto) {
        return ResultVO.success("新增成功", ruleConfigService.create(dto));
    }

    // ==================== 更新（新版本） ====================

    /**
     * 更新规则（生成新版本）
     *
     * 基于当前最大版本号 +1，旧版本保留不变
     */
    @PutMapping
    public ResultVO<WarningRuleConfigEntity> update(@Validated @RequestBody RuleConfigDTO dto) {
        return ResultVO.success("更新成功", ruleConfigService.update(dto));
    }

    // ==================== 删除（伪删除） ====================

    /**
     * 删除规则（伪删除）
     *
     * 将该 ruleCode 下所有版本的 deleted 标记为 1，不物理删除。
     * 已删除数据不会出现在常规查询结果中，但可通过 /versions/{ruleCode}/all 回溯历史
     */
    @DeleteMapping("/{ruleCode}")
    public ResultVO<Void> delete(@PathVariable String ruleCode) {
        ruleConfigService.delete(ruleCode);
        return ResultVO.success("删除成功");
    }

    // ==================== 生效/停用 ====================

    /**
     * 生效规则
     *
     * 将当前版本设为生效，同时归档旧版本
     */
    @PutMapping("/{id}/activate")
    public ResultVO<Void> activate(@PathVariable Long id) {
        ruleConfigService.activate(id);
        return ResultVO.success("生效成功");
    }

    /**
     * 停用规则
     */
    @PutMapping("/{id}/deactivate")
    public ResultVO<Void> deactivate(@PathVariable Long id) {
        ruleConfigService.deactivate(id);
        return ResultVO.success("停用成功");
    }

    // ==================== 查询 ====================

    /**
     * 查询单条规则详情
     */
    @GetMapping("/{id}")
    public ResultVO<WarningRuleConfigEntity> getById(@PathVariable Long id) {
        return ResultVO.success(ruleConfigService.getById(id));
    }

    /**
     * 按组合类型查询规则列表（组合维度视图）
     *
     * 对应原型 Page4：选择左侧组合类型树 → 右侧展示该类型下所有生效规则
     */
    @GetMapping("/portfolio/{portfolioTypeCode}")
    public ResultVO<List<WarningRuleConfigEntity>> listByPortfolioType(
            @PathVariable String portfolioTypeCode) {
        return ResultVO.success(ruleConfigService.listByPortfolioType(portfolioTypeCode));
    }

    /**
     * 按指标编码查询规则列表（指标维度视图）
     *
     * 对应原型 Page5：选择左侧指标树 → 右侧展示该指标在所有组合类型下的生效规则
     */
    @GetMapping("/indicator/{indicatorCode}")
    public ResultVO<List<WarningRuleConfigEntity>> listByIndicator(
            @PathVariable String indicatorCode) {
        return ResultVO.success(ruleConfigService.listByIndicator(indicatorCode));
    }

    /**
     * 查询规则的所有历史版本（不含已删除）
     */
    @GetMapping("/versions/{ruleCode}")
    public ResultVO<List<WarningRuleConfigEntity>> listVersions(@PathVariable String ruleCode) {
        return ResultVO.success(ruleConfigService.listVersions(ruleCode));
    }

    /**
     * 查询规则的所有历史版本（含已删除）
     *
     * 用于回溯已删除规则的历史版本
     */
    @GetMapping("/versions/{ruleCode}/all")
    public ResultVO<List<WarningRuleConfigEntity>> listVersionsIncludeDeleted(@PathVariable String ruleCode) {
        return ResultVO.success(ruleConfigService.listVersionsIncludeDeleted(ruleCode));
    }
}
