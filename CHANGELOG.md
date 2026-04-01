# 需求变更记录

> 每次需求变更按时间倒序追加，记录变更内容、原因及涉及文件。

---

## [v9] 2026-04-01 23:13 — 新增模拟数据 SQL

**需求**：根据当前代码逻辑生成模拟数据，展示数据库中的实际数据效果，并体现 AND 条件关系。

**变更**：
- 新建 `mock_data.sql`，包含：
  - **12 条指标元数据**（indicator_metadata）：覆盖业绩表现、风险指标、权益资产配置、交易行为四大类
  - **7 条规则（11 条记录含多版本）**（warning_rule_config），覆盖以下场景：
    - 单指标 SINGLE：规则1、规则4、规则6
    - **多指标 AND（2/3/4 个条件）**：规则2（3指标AND）、规则5（4指标AND）、规则7（2指标AND）
    - 版本演进：规则1（v1归档→v2生效）、规则4（v1→v2→v3删除）、规则6（v1生效→v2停用草稿）
    - 伪删除：规则4 v3（deleted=1），可通过 versions/all 回溯
    - 状态流转：DRAFT → ACTIVE → ARCHIVED / INACTIVE
  - 4 条验证查询 SQL

**涉及文件**：
- `mock_data.sql`（新增）

---

## [v8] 2026-04-01 23:06 — 新增全局异常处理与业务校验

**需求**：当前代码异常处理较少，需要加一些方便排错的异常处理。

**分析**：
- Service 层抛的是裸 `RuntimeException`，Controller 没有统一拦截，出异常就是 Spring 默认 500 白页
- 关键操作（更新、删除、生效、停用）缺少前置校验（如规则不存在、状态不允许操作等）

**变更**：
- 新建 `exception/BusinessException.java`：自定义业务异常，携带 code 和 message
- 新建 `exception/GlobalExceptionHandler.java`：全局异常处理器，拦截 5 类异常：
  - `BusinessException` → 业务错误，返回 code + message
  - `MethodArgumentNotValidException` → `@Validated` 校验失败，返回具体字段错误
  - `BindException` → 表单绑定错误
  - `MissingServletRequestParameterException` → 缺少必传参数
  - `Exception` → 兜底，日志记录完整堆栈，前端只看到通用提示
- ServiceImpl 关键位置补充业务校验：
  - `update`：规则不存在时抛异常（原来直接忽略）
  - `delete`：规则不存在时抛异常（原来静默成功）
  - `activate`：规则不存在 / 已是生效状态时抛异常
  - `deactivate`：规则不存在 / 已是停用状态时抛异常
- 所有 `RuntimeException` 统一替换为 `BusinessException`

**涉及文件**：
- `exception/BusinessException.java`（新增）
- `exception/GlobalExceptionHandler.java`（新增）
- `service/RuleConfigServiceImpl.java`

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
