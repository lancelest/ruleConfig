package com.risk.service;

import com.risk.dto.RuleConfigDTO;
import com.risk.entity.WarningRuleConfigEntity;

import java.util.List;

/**
 * 预警规则配置 Service 接口
 *
 * 定义规则配置的完整生命周期操作：
 * 新增 → 保存版本 → 生效 → 编辑 → 重新版本 → 伪删除
 */
public interface RuleConfigService {

    /**
     * 新增规则
     *
     * 逻辑：插入一条新记录，version=1，status=DRAFT，isCurrentVersion=0
     *
     * @param dto 请求参数
     * @return 新增的规则配置
     */
    WarningRuleConfigEntity create(RuleConfigDTO dto);

    /**
     * 更新规则（生成新版本）
     *
     * 逻辑：
     * 1. 查询当前规则的最大版本号
     * 2. 新版本 = 最大版本 + 1
     * 3. 插入新记录，status=DRAFT，isCurrentVersion=0
     * 旧版本不受影响，仍然保持原状态
     *
     * @param dto 请求参数
     * @return 新版本的规则配置
     */
    WarningRuleConfigEntity update(RuleConfigDTO dto);

    /**
     * 删除规则（伪删除）
     *
     * 将该 ruleCode 下所有版本的 deleted 标记为 1
     *
     * @param ruleCode 规则编码
     */
    void delete(String ruleCode);

    /**
     * 生效规则
     *
     * 将该 ruleCode 下旧版本的 isCurrentVersion 设为 0，status 改为 ARCHIVED
     * 将当前版本的 isCurrentVersion 设为 1，status 改为 ACTIVE
     *
     * @param id 当前要生效的规则配置ID
     */
    void activate(Long id);

    /**
     * 停用规则
     *
     * @param id 规则配置ID
     */
    void deactivate(Long id);

    /**
     * 查询单条规则
     *
     * @param id 规则配置ID
     * @return 规则配置详情
     */
    WarningRuleConfigEntity getById(Long id);

    /**
     * 按组合类型查询规则列表（组合维度视图）
     *
     * @param portfolioTypeCode 组合类型编码
     * @return 该类型下所有规则（仅返回当前生效版本）
     */
    List<WarningRuleConfigEntity> listByPortfolioType(String portfolioTypeCode);

    /**
     * 按指标编码查询规则列表（指标维度视图）
     *
     * @param indicatorCode 指标编码
     * @return 该指标在所有组合类型下的规则（仅返回当前生效版本）
     */
    List<WarningRuleConfigEntity> listByIndicator(String indicatorCode);

    /**
     * 查询规则的所有历史版本（不含已删除）
     *
     * @param ruleCode 规则编码
     * @return 按版本号倒序排列的所有版本
     */
    List<WarningRuleConfigEntity> listVersions(String ruleCode);

    /**
     * 查询规则的所有历史版本（含已删除）
     *
     * 绕过 @TableLogic 的自动过滤，用于回溯已删除的历史版本
     *
     * @param ruleCode 规则编码
     * @return 按版本号倒序排列的所有版本（包含已删除记录）
     */
    List<WarningRuleConfigEntity> listVersionsIncludeDeleted(String ruleCode);
}
