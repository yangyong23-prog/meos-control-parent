# 照明反馈调节校验简化 + 旧链路清理 实施计划

## 概要

两阶段执行：先清理旧死代码，再重构校验接口。

## 阶段一：旧链路死代码清理

### 1.1 `DemandLightingController.java` — 删除旧端点

**文件**: `saas/meos-control-server/src/main/java/com/persagy/apccontrol/controller/demand/lighting/DemandLightingController.java`

删除以下端点方法：
- `editSceneConfigs` (POST `/editSceneConfigs`)
- `editSceneObjectSettings` (POST `/editSceneObjectSettings`)
- `copySceneConfigByTemplate` (POST `/copySceneConfigByTemplate`)
- `copySceneConfig` (POST `/copySceneConfig`)
- `clear` (POST `/clear`)
- `removeCurrentCardConfig` (POST `/removeCurrentCardConfig`)

删除字段注入：`demandConfigLightingHandler`

### 1.2 `DemandLightingService.java` — 删除旧接口声明

**文件**: `common/src/main/java/com/persagy/apccontrol/basic/service/demand/lighting/DemandLightingService.java`

删除以下方法声明：
- `editSceneConfigs`
- `editSceneObjectSettings`
- `copySceneConfigByTemplate`
- `copySceneConfig`
- `removeDemandConfigLightingConfig`
- `clearDemandConfigLightingConfig`
- `removeCurrentCardConfig`
- `checkSceneName`（若确认死代码）

### 1.3 `DemandLightingServiceImpl.java` — 删除旧实现

**文件**: `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/service/demand/impl/DemandLightingServiceImpl.java`

删除以下方法实现：
- `editSceneConfigs` + 私有辅助 `saveOrUpdateSceneConfig`
- `editSceneObjectSettings`
- `copySceneConfigByTemplate`
- `copySceneConfig`
- `removeDemandConfigLightingConfig`
- `clearDemandConfigLightingConfig`
- `removeCurrentCardConfig`
- `checkSceneName`（若确认死代码）
- 私有辅助 `removeDemandLightingResultRecord`
- 私有辅助 `resultComputeDemandLightingResult(Long templateId, Long sceneId)`（仅旧方法调用）

删除字段注入（仅旧方法使用的）：
- `demandLightingStrategy`
- `demandLightingQueryService`
- `demandConfigLightingService`
- `demandConfigLightingSceneService`
- `demandConfigLightingResultService`
- `demandConfigLightingHandler`

删除旧 import 语句。

**保留**的方法：
- `migrateSceneData` / `migrateConfigData`
- `onDemandConfigChangeEvent`（事件监听，使用新网关）
- `resultComputeDemandLightingResult(RunningMonitorSecondTypeEnum, Long)`（外部调用）
- `feedbackConsistencyCheck`（重构目标）
- `logTag`

**保留**的字段注入：
- `demandMigrateLightingSceneTask`
- `demandMigrateLightingConfigTask`
- `demandConfigLightingComputeNowService`
- `demandTemplateRepository`
- `feedbackConsistencyValidator`
- `dtpRwdClientService`

## 阶段二：反馈调节校验接口重构

> **入参不变**：`DemandAddParam` 与新保存链路 `DemandController.addDemandConfig` 入参一致，前端保存时调用此接口校验。

### 2.1 `DemandLightingServiceImpl.java` — 简化 `feedbackConsistencyCheck`

简化实现：
1. 保留现有入参 `DemandAddParam`，保留 `analyzeConfig` 解析逻辑
2. 从解析后的 `DemandConfigLightingValueObject` 中提取请求中 FEEDBACK_ADJUSTMENT 的回路→条件
3. 调用 `feedbackConsistencyValidator` 做**全空间**一致性比对（请求空间 + DB 中其他空间一起比）
4. 回填回路名称和空间名称

### 2.4 `DemandLightingFeedbackConsistencyValidator.java` — 简化校验器

**文件**: `common/src/main/java/com/persagy/apccontrol/basic/service/demand/validator/DemandLightingFeedbackConsistencyValidator.java`

核心逻辑简化为：「查询同模板同场景下所有配置了该回路 FEEDBACK_ADJUSTMENT 的空间 → 比对条件是否一致 → 不一致则返回全部空间配置」

简化：
1. `feedbackConsistencyCheck` 方法：
   - 入参：`templateId` + `sceneId` + 请求中各回路的目标条件
   - 查询 `demand_config` 表：同模板同场景下，所有配置了该回路 FEEDBACK_ADJUSTMENT 的空间（**不排除自身**）
   - 将请求中的回路条件与 DB 中所有空间的条件合并后统一比对
   - 不一致则返回该回路**全部**空间配置（含请求空间和 DB 空间）
2. 移除 `extractPerSpaceCircuits` 方法（不再需要）
3. 移除 `extractCircuitsFromValueObject` 重载方法（不再需要解析 ValueObject）
4. 移除 `validate` 方法（不再需要单独的保存时校验，由 `beforeAddConfigHandler` 统一处理）
5. 保留 `extractOtherInstanceCircuits`，改为查询全部空间（不排除自身）

### 2.5 `AbstractDemandConfigLightingAnalyzeStrategy.java` — 保存时校验增强

**文件**: `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/strategy/config/lighting/AbstractDemandConfigLightingAnalyzeStrategy.java`

修改 `beforeAddConfigHandler`：
- 不再抛出通用 `ValidationException`，改为返回冲突详情（或抛一个携带冲突详情的特定异常）
- 确保并发场景下能返回冲突回路及冲突空间配置

## 编译验证

```bash
mvn compile -pl common,saas/meos-control-common,saas/meos-control-server -am -q
```

## 风险点

1. `checkSceneName` 需确认是否被前端调用（不在 Controller 中，但可能在 Service 层被引用）
2. `demandSceneGateway` 和 `demandConfigGateway` 字段在移除旧方法后是否还被使用，需确认
3. `DemandLightingStrategy` 和 `DemandLightingQueryService` 在删除旧方法后是否还被其他保留方法引用