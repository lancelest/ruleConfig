# 需求变更记录

> 每次需求变更按时间倒序追加，记录变更内容、原因及涉及文件。

---

## [v7] 2026-04-01 22:55 — 合并删除接口为单一入口

**需求**：`deleteById` 和 `deleteByRuleCode` 两个删除接口暴露给前端会造成困惑，合并为一个。

**分析**：
- `deleteById` 只删单条版本记录，会导致同一 ruleCode 下版本链断裂（v1 未删、v2 已删、v3 未删），业务语义不合理
- 规则的删除应该是针对 `ruleCode` 的整体操作，不应允许删除单个版本

**变更**：
- `DELETE /{id}` 和 `DELETE /code/{ruleCode}` → 合并为 `DELETE /{ruleCode}`
- Service 接口：`deleteById` + `deleteByRuleCode` → `delete(String ruleCode)`
- ServiceImpl：只保留 delete 实现，按 ruleCode 批量标记 `deleted=1`

**涉及文件**：
- `controller/RuleConfigController.java`
- `service/RuleConfigService.java`
- `service/RuleConfigServiceImpl.java`

---

## [v6] 支持查看已删除规则的历史版本

**需求**：规则被伪删除后，希望某天仍能回溯查看历史版本。

**变更**：
- Mapper 新增 `@Select` 自定义 SQL，绕过 `@TableLogic` 自动过滤，查询包含已删除记录
- Service 新增 `listVersionsIncludeDeleted` 方法
- Controller 新增 `GET /api/rule-config/versions/{ruleCode}/all` 接口

**涉及文件**：
- `mapper/WarningRuleConfigMapper.java`
- `service/RuleConfigService.java`
- `service/RuleConfigServiceImpl.java`
- `controller/RuleConfigController.java`

---

## [v5] 实体类命名规范化

**需求**：实体类名统一加 `Entity` 后缀。

**变更**：
- `WarningRuleConfig` → `WarningRuleConfigEntity`
- `IndicatorMetadata` → `IndicatorMetadataEntity`
- 所有引用类同步更新

**涉及文件**：所有 Java 文件（引用了实体类的 Controller、Service、ServiceImpl、Mapper 等）

---

## [v4] 合并 CreateDTO 和 UpdateDTO

**需求**：新建和更新共用一个 DTO，不需要分开。

**变更**：
- 删除 `RuleConfigCreateDTO` 和 `RuleConfigUpdateDTO`
- 合并为单一 `RuleConfigDTO`，同时用于新增和更新接口

**涉及文件**：
- `dto/RuleConfigDTO.java`
- `controller/RuleConfigController.java`
- `service/RuleConfigService.java`
- `service/RuleConfigServiceImpl.java`

---

## [v3] 修复 ResultVO 泛型类型推断冲突

**问题**：`ResultVO.success("删除成功")` 导致编译错误，Java 泛型推断上限 `Void` 与下限 `String` 不兼容。

**变更**：重构 ResultVO 的四个 success 重载方法，明确区分无数据、纯消息、带数据三种场景。

**涉及文件**：
- `dto/ResultVO.java`

---

## [v2] 修复 Maven 构建与 IDEA 环境问题

**问题**：多处环境配置问题导致编译失败。

**变更**：
- MySQL 驱动从 `mysql:mysql-connector-java` 改为 `com.mysql:mysql-connector-j`
- `java.version` 从 `13.0.2` 改为 `13`（Maven 只接受大版本号）
- MyBatis-Plus 版本从 `3.5.3.1` 升级到 `3.5.5`（适配 JDK 13）
- 删除 `.idea` 目录，重新从 pom.xml 导入 IDEA 项目

**涉及文件**：
- `pom.xml`

---

## [v1] 重构包结构：修复非法包名 + 扁平化

**问题**：原包名 `com.tonghua.risk.interface` 中 `interface` 是 Java 关键字，且层级嵌套过深。

**变更**：
- 整体包名从 `com.tonghua.risk.interface` 改为 `com.risk`
- 扁平化子包：`controller/`、`service/`、`entity/`、`dto/`、`vo/`、`mapper/`、`enums/`、`handler/`
- `pom.xml` 的 `groupId` 同步改为 `com.risk`
- 全部 17 个 Java 文件重新创建

**涉及文件**：所有 Java 文件 + `pom.xml`
