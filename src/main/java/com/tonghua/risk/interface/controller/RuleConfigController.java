package com.tonghua.risk.interface.controller;

import com.tonghua.risk.application.service.RuleConfigService;
import com.tonghua.risk.domain.entity.WarningRuleConfig;
import com.tonghua.risk.interface.dto.ResultVO;
import com.tonghua.risk.interface.dto.RuleConfigCreateDTO;
import com.tonghua.risk.interface.dto.RuleConfigUpdateDTO;
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
    public ResultVO<WarningRuleConfig> create(@Validated @RequestBody RuleConfigCreateDTO dto) {
        return ResultVO.success("新增成功", ruleConfigService.create(dto));
    }

    // ==================== 更新（新版本） ====================

    /**
     * 更新规则（生成新版本）
     *
     * 基于当前最大版本号 +1，旧版本保留不变
     */
    @PutMapping
    public ResultVO<WarningRuleConfig> update(@Validated @RequestBody RuleConfigUpdateDTO dto) {
        return ResultVO.success("更新成功", ruleConfigService.update(dto));
    }

    // ==================== 删除（伪删除） ====================

    /**
     * 按ID伪删除
     *
     * 仅标记 deleted=1，不物理删除，已删除数据不会出现在任何查询结果中
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteById(@PathVariable Long id) {
        ruleConfigService.deleteById(id);
        return ResultVO.success("删除成功");
    }

    /**
     * 按规则编码伪删除（删除该规则所有版本）
     *
     * 将该 ruleCode 下所有版本的 deleted 标记为 1
     */
    @DeleteMapping("/code/{ruleCode}")
    public ResultVO<Void> deleteByRuleCode(@PathVariable String ruleCode) {
        ruleConfigService.deleteByRuleCode(ruleCode);
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
    public ResultVO<WarningRuleConfig> getById(@PathVariable Long id) {
        return ResultVO.success(ruleConfigService.getById(id));
    }

    /**
     * 按组合类型查询规则列表（组合维度视图）
     *
     * 对应原型 Page4：选择左侧组合类型树 → 右侧展示该类型下所有生效规则
     */
    @GetMapping("/portfolio/{portfolioTypeCode}")
    public ResultVO<List<WarningRuleConfig>> listByPortfolioType(
            @PathVariable String portfolioTypeCode) {
        return ResultVO.success(ruleConfigService.listByPortfolioType(portfolioTypeCode));
    }

    /**
     * 按指标编码查询规则列表（指标维度视图）
     *
     * 对应原型 Page5：选择左侧指标树 → 右侧展示该指标在所有组合类型下的生效规则
     */
    @GetMapping("/indicator/{indicatorCode}")
    public ResultVO<List<WarningRuleConfig>> listByIndicator(
            @PathVariable String indicatorCode) {
        return ResultVO.success(ruleConfigService.listByIndicator(indicatorCode));
    }

    /**
     * 查询规则的所有历史版本
     */
    @GetMapping("/versions/{ruleCode}")
    public ResultVO<List<WarningRuleConfig>> listVersions(@PathVariable String ruleCode) {
        return ResultVO.success(ruleConfigService.listVersions(ruleCode));
    }
}
