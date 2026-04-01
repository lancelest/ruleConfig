-- ============================================================
-- 预警规则配置 - 模拟数据
-- 覆盖场景：单指标SINGLE、多指标AND、版本演进、伪删除、状态流转
-- ============================================================

-- 先清空（按顺序，注意外键依赖）
DELETE FROM warning_rule_config;
DELETE FROM indicator_metadata;

-- ============================================================
-- 一、指标元数据（indicator_metadata）
-- ============================================================
INSERT INTO indicator_metadata (indicator_code, indicator_name, category_code, category_name, calc_formula) VALUES
('excess_return_1y',     '超额收益_近一年',       'performance',       '业绩表现',     '近一年收益率 - 基准近一年收益率'),
('excess_return_3y',     '超额收益_近三年',       'performance',       '业绩表现',     '近三年收益率 - 基准近三年收益率'),
('max_drawdown_1y',      '最大回撤_近一年',       'risk',              '风险指标',     '近一年最大回撤（峰值到谷底的最大跌幅）'),
('max_drawdown_3y',      '最大回撤_近三年',       'risk',              '风险指标',     '近三年最大回撤'),
('stock_position_ratio', '股票仓位比例',          'equity_allocation', '权益资产配置', '股票资产市值 / 基金总资产'),
('bond_position_ratio',  '债券仓位比例',          'equity_allocation', '权益资产配置', '债券资产市值 / 基金总资产'),
('stock_position_deviation', '股票仓位偏离度',    'equity_allocation', '权益资产配置', '|实际仓位 - 基准仓位|'),
('benchmark_risk_ratio', '基准风险比例',          'risk',              '风险指标',     '组合跟踪误差 / 基准波动率'),
('sharpe_ratio_1y',      '夏普比率_近一年',       'performance',       '业绩表现',     '(年化超额收益 - 无风险利率) / 年化跟踪误差'),
('volatility_1y',        '波动率_近一年',         'risk',              '风险指标',     '近一年日收益率的标准差 × √252'),
('concentration_top10',  '前十大重仓股集中度',     'risk',              '风险指标',     '前十大重仓股市值 / 股票总市值'),
('turnover_rate_1y',     '换手率_近一年',         'trading',           '交易行为',     '近一年买入/卖出总金额 / 平均资产净值');


-- ============================================================
-- 二、规则配置（warning_rule_config）
-- ============================================================

-- -------------------------------------------------------
-- 规则1：单指标规则 — 超额收益_近一年（主动权益）
-- 演示：logic=SINGLE，简单条件
-- 版本演进：v1 草稿 → v2 修改 → v2 生效
-- -------------------------------------------------------
INSERT INTO warning_rule_config
(id, rule_code, rule_name, portfolio_type_code, portfolio_type_name,
 indicator_code, indicator_name, sub_category, rule_conditions,
 threshold_l1, threshold_l2, threshold_l3, threshold_l4,
 version, status, is_current_version, deleted,
 effective_time, expire_time, create_by, create_time, update_by, update_time)
VALUES
-- v1：草稿（已归档）
(1, 'RULE_EXCESS_RET_AE', '主动权益-超额收益近一年预警', 'active_equity', '主动权益',
 'excess_return_1y', '超额收益_近一年', NULL,
 '{"logic":"SINGLE","conditions":[{"indicatorCode":"excess_return_1y","indicatorName":"超额收益_近一年","operator":"GT","threshold":0.03}]}',
 0.03, -0.02, -0.05, -0.10,
 1, 'ARCHIVED', 0, 0,
 NULL, NULL, '张三', '2026-03-20 10:00:00', '张三', '2026-03-22 14:00:00'),

-- v2：修改阈值后生效（当前版本）
(2, 'RULE_EXCESS_RET_AE', '主动权益-超额收益近一年预警', 'active_equity', '主动权益',
 'excess_return_1y', '超额收益_近一年', NULL,
 '{"logic":"SINGLE","conditions":[{"indicatorCode":"excess_return_1y","indicatorName":"超额收益_近一年","operator":"GT","threshold":0.05}]}',
 0.05, -0.03, -0.05, -0.10,
 2, 'ACTIVE', 1, 0,
 '2026-03-22 14:00:00', NULL, '张三', '2026-03-22 14:00:00', '张三', '2026-03-22 14:00:00');


-- -------------------------------------------------------
-- 规则2：多指标AND规则 — 主动权益综合风控（核心AND示例）
-- 演示：logic=AND，三个指标同时满足才触发
-- 含义：超额收益>0 AND 基准风险比例<10% AND 股票仓位偏离度>5%
-- -------------------------------------------------------
INSERT INTO warning_rule_config
(id, rule_code, rule_name, portfolio_type_code, portfolio_type_name,
 indicator_code, indicator_name, sub_category, rule_conditions,
 threshold_l1, threshold_l2, threshold_l3, threshold_l4,
 version, status, is_current_version, deleted,
 effective_time, expire_time, create_by, create_time, update_by, update_time)
VALUES
(3, 'RULE_COMPREHENSIVE_AE', '主动权益-综合风控预警', 'active_equity', '主动权益',
 'excess_return_1y', '超额收益_近一年', '综合风控',
 '{"logic":"AND","conditions":['
   '{"indicatorCode":"excess_return_1y","indicatorName":"超额收益_近一年","operator":"GT","threshold":0},'
   '{"indicatorCode":"benchmark_risk_ratio","indicatorName":"基准风险比例","operator":"LT","threshold":0.10},'
   '{"indicatorCode":"stock_position_deviation","indicatorName":"股票仓位偏离度","operator":"GT","threshold":0.05}'
 ']}',
 0.05, -0.03, -0.08, -0.15,
 1, 'ACTIVE', 1, 0,
 '2026-03-25 09:00:00', NULL, '李四', '2026-03-25 09:00:00', '李四', '2026-03-25 09:00:00');


-- -------------------------------------------------------
-- 规则3：多指标AND规则 — 债券仓位+偏离度双条件（被动指数）
-- 含义：债券仓位比例>30% AND 股票仓位偏离度>8%
-- -------------------------------------------------------
INSERT INTO warning_rule_config
(id, rule_code, rule_name, portfolio_type_code, portfolio_type_name,
 indicator_code, indicator_name, sub_category, rule_conditions,
 threshold_l1, threshold_l2, threshold_l3, threshold_l4,
 version, status, is_current_version, deleted,
 effective_time, expire_time, create_by, create_time, update_by, update_time)
VALUES
(4, 'RULE_BOND_STOCK_PI', '被动指数-债券仓位与偏离度双条件预警', 'passive_index', '被动指数',
 'bond_position_ratio', '债券仓位比例', '仓位双条件',
 '{"logic":"AND","conditions":['
   '{"indicatorCode":"bond_position_ratio","indicatorName":"债券仓位比例","operator":"GT","threshold":0.30},'
   '{"indicatorCode":"stock_position_deviation","indicatorName":"股票仓位偏离度","operator":"GT","threshold":0.08}'
 ']}',
 0.35, 0.40, 0.45, 0.50,
 1, 'ACTIVE', 1, 0,
 '2026-03-26 11:00:00', NULL, '王五', '2026-03-26 11:00:00', '王五', '2026-03-26 11:00:00');


-- -------------------------------------------------------
-- 规则4：单指标 — 最大回撤_近一年（混合型）
-- 版本演进：v1 → v2 → v3，v3 已删除（伪删除）
-- -------------------------------------------------------
INSERT INTO warning_rule_config
(id, rule_code, rule_name, portfolio_type_code, portfolio_type_name,
 indicator_code, indicator_name, sub_category, rule_conditions,
 threshold_l1, threshold_l2, threshold_l3, threshold_l4,
 version, status, is_current_version, deleted,
 effective_time, expire_time, create_by, create_time, update_by, update_time)
VALUES
-- v1：草稿
(5, 'RULE_MAX_DD_HY', '混合型-最大回撤近一年预警', 'hybrid', '混合型',
 'max_drawdown_1y', '最大回撤_近一年', NULL,
 '{"logic":"SINGLE","conditions":[{"indicatorCode":"max_drawdown_1y","indicatorName":"最大回撤_近一年","operator":"ABS_GT","threshold":0.10}]}',
 -0.10, -0.15, -0.20, -0.30,
 1, 'DRAFT', 0, 0,
 NULL, NULL, '张三', '2026-03-15 09:00:00', '张三', '2026-03-15 09:00:00'),

-- v2：修改阈值
(6, 'RULE_MAX_DD_HY', '混合型-最大回撤近一年预警', 'hybrid', '混合型',
 'max_drawdown_1y', '最大回撤_近一年', NULL,
 '{"logic":"SINGLE","conditions":[{"indicatorCode":"max_drawdown_1y","indicatorName":"最大回撤_近一年","operator":"ABS_GT","threshold":0.15}]}',
 -0.15, -0.20, -0.25, -0.35,
 2, 'DRAFT', 0, 0,
 NULL, NULL, '张三', '2026-03-18 14:00:00', '张三', '2026-03-18 14:00:00'),

-- v3：继续修改，后被删除（伪删除 deleted=1）
(7, 'RULE_MAX_DD_HY', '混合型-最大回撤近一年预警', 'hybrid', '混合型',
 'max_drawdown_1y', '最大回撤_近一年', NULL,
 '{"logic":"SINGLE","conditions":[{"indicatorCode":"max_drawdown_1y","indicatorName":"最大回撤_近一年","operator":"ABS_GT","threshold":0.12}]}',
 -0.12, -0.18, -0.25, -0.30,
 3, 'DRAFT', 0, 1,
 NULL, NULL, '张三', '2026-03-28 16:00:00', '张三', '2026-03-30 10:00:00');


-- -------------------------------------------------------
-- 规则5：多指标AND — 主动权益风险组合（四指标AND）
-- 含义：最大回撤>15% AND 波动率>20% AND 夏普比率<0.5 AND 集中度>60%
-- -------------------------------------------------------
INSERT INTO warning_rule_config
(id, rule_code, rule_name, portfolio_type_code, portfolio_type_name,
 indicator_code, indicator_name, sub_category, rule_conditions,
 threshold_l1, threshold_l2, threshold_l3, threshold_l4,
 version, status, is_current_version, deleted,
 effective_time, expire_time, create_by, create_time, update_by, update_time)
VALUES
(8, 'RULE_MULTI_RISK_AE', '主动权益-多指标风险组合预警', 'active_equity', '主动权益',
 'max_drawdown_1y', '最大回撤_近一年', '多维度风控',
 '{"logic":"AND","conditions":['
   '{"indicatorCode":"max_drawdown_1y","indicatorName":"最大回撤_近一年","operator":"ABS_GT","threshold":0.15},'
   '{"indicatorCode":"volatility_1y","indicatorName":"波动率_近一年","operator":"GT","threshold":0.20},'
   '{"indicatorCode":"sharpe_ratio_1y","indicatorName":"夏普比率_近一年","operator":"LT","threshold":0.50},'
   '{"indicatorCode":"concentration_top10","indicatorName":"前十大重仓股集中度","operator":"GT","threshold":0.60}'
 ']}',
 0.60, 0.65, 0.70, 0.80,
 1, 'ACTIVE', 1, 0,
 '2026-04-01 10:00:00', NULL, '李四', '2026-04-01 10:00:00', '李四', '2026-04-01 10:00:00');


-- -------------------------------------------------------
-- 规则6：单指标 — 换手率（主动权益），已停用
-- -------------------------------------------------------
INSERT INTO warning_rule_config
(id, rule_code, rule_name, portfolio_type_code, portfolio_type_name,
 indicator_code, indicator_name, sub_category, rule_conditions,
 threshold_l1, threshold_l2, threshold_l3, threshold_l4,
 version, status, is_current_version, deleted,
 effective_time, expire_time, create_by, create_time, update_by, update_time)
VALUES
(9, 'RULE_TURNOVER_AE', '主动权益-换手率预警', 'active_equity', '主动权益',
 'turnover_rate_1y', '换手率_近一年', NULL,
 '{"logic":"SINGLE","conditions":[{"indicatorCode":"turnover_rate_1y","indicatorName":"换手率_近一年","operator":"GT","threshold":3.00}]}',
 3.00, 5.00, 8.00, 10.00,
 1, 'ACTIVE', 1, 0,
 '2026-03-10 09:00:00', '2026-03-28 18:00:00', '张三', '2026-03-10 09:00:00', '张三', '2026-03-28 18:00:00'),

-- 停用后重新编辑产生 v2（草稿）
(10, 'RULE_TURNOVER_AE', '主动权益-换手率预警', 'active_equity', '主动权益',
 'turnover_rate_1y', '换手率_近一年', NULL,
 '{"logic":"SINGLE","conditions":[{"indicatorCode":"turnover_rate_1y","indicatorName":"换手率_近一年","operator":"GT","threshold":4.00}]}',
 4.00, 6.00, 8.00, 12.00,
 2, 'INACTIVE', 0, 0,
 NULL, NULL, '张三', '2026-03-28 18:00:00', '张三', '2026-03-28 18:00:00');


-- -------------------------------------------------------
-- 规则7：多指标AND — 被动指数超额收益+风险双条件
-- 含义：超额收益_近三年>0 AND 基准风险比例<5%
-- -------------------------------------------------------
INSERT INTO warning_rule_config
(id, rule_code, rule_name, portfolio_type_code, portfolio_type_name,
 indicator_code, indicator_name, sub_category, rule_conditions,
 threshold_l1, threshold_l2, threshold_l3, threshold_l4,
 version, status, is_current_version, deleted,
 effective_time, expire_time, create_by, create_time, update_by, update_time)
VALUES
(11, 'RULE_EXCESS_RISK_PI', '被动指数-超额收益与风险双条件预警', 'passive_index', '被动指数',
 'excess_return_3y', '超额收益_近三年', '收益与风险',
 '{"logic":"AND","conditions":['
   '{"indicatorCode":"excess_return_3y","indicatorName":"超额收益_近三年","operator":"GT","threshold":0},'
   '{"indicatorCode":"benchmark_risk_ratio","indicatorName":"基准风险比例","operator":"LT","threshold":0.05}'
 ']}',
 0.02, -0.02, -0.05, -0.10,
 1, 'ACTIVE', 1, 0,
 '2026-03-27 15:00:00', NULL, '王五', '2026-03-27 15:00:00', '王五', '2026-03-27 15:00:00');


-- ============================================================
-- 三、查询验证
-- ============================================================

-- 1. 查看所有规则及其版本
SELECT id, rule_code, rule_name, version, status, is_current_version, deleted,
       portfolio_type_name, indicator_name
FROM warning_rule_config
ORDER BY rule_code, version;


-- 2. 查看当前所有生效规则
SELECT id, rule_code, rule_name, version, status, portfolio_type_name,
       JSON_PRETTY(rule_conditions) AS conditions_json
FROM warning_rule_config
WHERE is_current_version = 1 AND status = 'ACTIVE' AND deleted = 0
ORDER BY id;


-- 3. 查看所有 AND 类型的规则（多指标组合）
SELECT id, rule_code, rule_name, version, status,
       JSON_EXTRACT(rule_conditions, '$.logic') AS logic_type,
       JSON_LENGTH(rule_conditions, '$.conditions') AS condition_count,
       portfolio_type_name
FROM warning_rule_config
WHERE deleted = 0
  AND JSON_EXTRACT(rule_conditions, '$.logic') = 'AND'
ORDER BY id;


-- 4. 查看 RULE_MAX_DD_HY 的所有版本（含已删除）— 验证伪删除回溯
SELECT id, rule_code, rule_name, version, status, deleted, create_time
FROM warning_rule_config
WHERE rule_code = 'RULE_MAX_DD_HY'
ORDER BY version DESC;
