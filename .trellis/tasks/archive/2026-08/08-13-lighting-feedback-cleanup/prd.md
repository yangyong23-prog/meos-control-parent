# 照明反馈调节校验简化 + 旧链路清理 PRD

## 需求概述

重构 `/demand/lighting/check/feedbackConfig` 接口的校验逻辑，使其符合产品文档要求且代码实现更简洁；同步清理 `demand_config_lighting` 旧链路的死代码。

## 需求详情

### 1. 反馈调节一致性校验（/demand/lighting/check/feedbackConfig）

**触发时机**：用户点击页面"保存"时调用，不做即时校验。

**校验逻辑**：
- 同模板（templateId）+ 同场景（sceneId）下，查询 DB 中**所有**配置了该回路 FEEDBACK_ADJUSTMENT 的空间（含请求空间自身）
- 将请求中该回路的条件与 DB 中所有空间的条件**合并统一比对**，若存在不一致则返回该回路全部空间配置
- **排除规则**：某空间配置为该回路**不参与反馈调节**（settingMode ≠ FEEDBACK_ADJUSTMENT）时，不纳入校验
- 条件一致性定义：
  - 条件类型一致性：自动开启条件和自动关闭条件是否都配置了（或都没配置）
  - 阈值一致性：`underOutdoorIlluminance`（自动开启照度阈值）、`overtopOutdoorIlluminance`（自动关闭照度阈值）一致
  - 持续时间一致性：`underDuration`、`overtopDuration` 一致

**冲突响应格式**：
- 按回路返回冲突，每个回路的 `spaceConfigs` 按**所有**空间（含请求空间和 DB 空间）的配置作为一条展示
- 每条包含：回路名称、空间名称、具体的自动开启/关闭条件

**并发处理**：
- 保存时后端需基于最新 DB 配置再次校验
- 若保存前已有其他用户提交了不一致配置，则当前保存失败，并返回冲突回路及冲突空间配置

### 2. 旧链路清理

清理所有操作 `demand_config_lighting` 表（含 `demand_config_lighting_scene`、`demand_config_lighting_result`）的死代码，这些前端已不再调用。

## 接受标准

1. `/demand/lighting/check/feedbackConfig` 逻辑简化：
   - 入参不变（`DemandAddParam`，与新保存链路一致）
   - 校验器：同模板同场景下，查所有 FEEDBACK_ADJUSTMENT 空间（不排除自身），统一比对，不一致则返回全部空间配置
   - 校验器查询 `demand_config` 表（新表），不再查询旧表
2. 旧链路死代码删除：
   - `DemandLightingServiceImpl` 中操作旧表的 7 个方法及其私有辅助方法删除
   - `DemandLightingService` 接口中对应方法声明删除
   - `DemandLightingController` 中对应端点删除
3. 编译通过，无引用错误