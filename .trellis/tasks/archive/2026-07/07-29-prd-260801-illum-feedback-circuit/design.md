# 技术设计：PRD-26.0801 照度反馈条件按回路维度配置

## 1. 整体架构

### 1.1 现状架构

```
配置层 (common):
  DemandConfigLightingFuncTypeValueObject
    ├── spaceFeedbackAdjustType (空间级反馈方式: 室内/室外)
    ├── spaceFeedbackAdjustSettings (空间级反馈条件: allFeedback + 阈值/持续时间)
    └── circuitSettings: List<DemandConfigLightingCircuitValueObject>
          └── circuitId + settingMode (OPEN/CLOSE/FEEDBACK_ADJUSTMENT/NOT_CONFIGURED)

下发层 (compute-edge):
  LightingFeedbackAdjustTask
    ├── 遍历维度: 空间
    ├── 写入对象: 空间对象
    └── 写入内容: illumFbCtrType + 4个条件信息点 (全部写空间)

保存层 (saas):
  DemandLightingServiceImpl.editSceneObjectSettings()
    └── DemandLightingTemplateSceneConfigSaveParam.spaceFeedbackAdjustSettings (空间级)
```

### 1.2 目标架构

```
配置层 (common):
  DemandConfigLightingFuncTypeValueObject
    ├── spaceFeedbackAdjustType (空间级反馈方式: 室内/室外, 不变)
    ├── spaceFeedbackAdjustSettings → 废弃 allFeedback, 条件下沉到回路
    └── circuitSettings: List<DemandConfigLightingCircuitValueObject>
          ├── circuitId + settingMode
          ├── underOutdoorIlluminance + underDuration (自动开启条件)
          └── overtopOutdoorIlluminance + overtopDuration (自动关闭条件)

下发层 (compute-edge):
  LightingFeedbackAdjustTask
    ├── 空间指令: illumFbCtrType → 写入空间对象 (不变)
    ├── 回路指令: 4个条件信息点 → 写入回路对象 LTLTCC (新增)
    └── Redis 缓存: 从空间 ID 改为回路 ID

保存层 (saas):
  DemandLightingServiceImpl.editSceneObjectSettings()
    ├── 新增多空间回路一致性校验
    └── 并发保存冲突检测
```

## 2. 配置模型变更 (common 模块)

### 2.1 DemandConfigLightingCircuitValueObject 扩展

**文件**: `common/.../structure/demand/model/valueobject/lighting/children/DemandConfigLightingCircuitValueObject.java`

新增 `feedbackAdjustSetting` 对象字段（类型为新增的 `DemandLightingCircuitFeedbackAdjustSettingDTO`），当 `settingMode = FEEDBACK_ADJUSTMENT` 时携带反馈条件：

```java
// 回路级反馈调节条件配置
private DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSetting;
```

`DemandLightingCircuitFeedbackAdjustSettingDTO` 结构：
```java
private BigDecimal underOutdoorIlluminance;  // 自动开启条件照度阈值, 0-10000 正整数
private Integer underDuration;                 // 自动开启条件持续时间, 0-300 整数
private BigDecimal overtopOutdoorIlluminance; // 自动关闭条件照度阈值, 0-10000 正整数
private Integer overtopDuration;               // 自动关闭条件持续时间, 0-300 整数
```

新增 `checkAndSet()` 方法校验：
- `FEEDBACK_ADJUSTMENT` 时：`feedbackAdjustSetting` 非空，自动开启和自动关闭条件最少配置一项
- 两个条件都配置时：`underOutdoorIlluminance < overtopOutdoorIlluminance`
- `OPEN`/`CLOSE` 时：`feedbackAdjustSetting` 置空

### 2.2 DemandConfigLightingFeedbackAdjustDetailValueObject 简化

**文件**: `common/.../structure/demand/model/valueobject/lighting/children/DemandConfigLightingFeedbackAdjustDetailValueObject.java`

- **废弃** `allFeedback` 字段（不再有"全部反馈"概念，每回路独立配置）
- **废弃** `underOutdoorIlluminance`/`underDuration`/`overtopOutdoorIlluminance`/`overtopDuration`（条件下沉到回路）
- 保留类存在但仅作为空间级反馈方式的载体，或直接将 `spaceFeedbackAdjustType` 内联到 `DemandConfigLightingFuncTypeValueObject`

### 2.3 DemandConfigLightingFuncTypeValueObject 调整

**文件**: `common/.../structure/demand/model/valueobject/lighting/children/DemandConfigLightingFuncTypeValueObject.java`

- `spaceFeedbackAdjustType` 字段保留（空间级反馈方式不变）
- `spaceFeedbackAdjustSettings` 字段废弃或置空
- `checkAndSet()` 中 `FEEDBACK_ADJUSTMENT` case 调整：
  - 移除 `allFeedback` 判断逻辑
  - 改为遍历 `circuitSettings`，对 `settingMode = FEEDBACK_ADJUSTMENT` 的回路调用回路级 `checkAndSet()`

### 2.4 DemandLightingCircuitSettingDTO 扩展

**文件**: `common/.../model/dto/demand/lighting/DemandLightingCircuitSettingDTO.java`

与值对象同步新增 `feedbackAdjustSetting` 字段（类型 `DemandLightingCircuitFeedbackAdjustSettingDTO`），用于保存接口参数传递。

### 2.5 DemandLightingTemplateSceneConfigSaveParam 调整

**文件**: `common/.../model/param/demand/lighting/DemandLightingTemplateSceneConfigSaveParam.java`

- `spaceFeedbackAdjustSettings` 字段废弃条件值，回路条件通过 `circuitSettings[].feedbackAdjustSetting` 传递

### 2.6 DemandConfigLightingResultDataDTO 调整

**文件**: `common/.../model/dto/demand/lighting/DemandConfigLightingResultDataDTO.java`

- `feedbackAdjustSettings` 字段类型从 `DemandLightingSpaceFeedbackAdjustSettingDTO`（空间级）改为 `DemandLightingCircuitFeedbackAdjustSettingDTO`（回路级）
- `DemandConfigLightingComputeNowServiceImpl.putCircuitIdResults()` 需要将回路级条件从配置传递到结果

## 3. 多空间回路一致性校验 (saas 模块)

### 3.1 校验位置

**文件**: `saas/meos-control-common/.../service/demand/impl/DemandLightingServiceImpl.java`

在 `editSceneObjectSettings()` 方法中，配置校验通过后、持久化之前新增一致性校验。

### 3.2 校验流程

```
1. 从当前保存参数中提取所有 settingMode=FEEDBACK_ADJUSTMENT 的回路及其条件
2. 查询当前模板+场景下其他空间的配置（通过 demandConfigGateway）
3. 对每个当前空间中的反馈调节回路：
   a. 查询该回路服务的其他空间（通过 demandLightingStrategy.listSpacesByCircuit）
   b. 检查其他空间中该回路是否也配置为 FEEDBACK_ADJUSTMENT
   c. 若都为 FEEDBACK_ADJUSTMENT，比较条件是否一致
   d. 不一致则抛出异常，返回冲突回路和各空间配置详情
```

### 3.3 并发保存冲突处理

- 保存前基于最新配置再次校验（在事务内查询最新数据）
- 若保存前已有其他用户提交了不一致配置，当前保存失败
- 返回冲突回路 ID、回路名称、冲突空间 ID、空间名称及各自配置

### 3.4 校验服务设计

新增 `DemandLightingFeedbackConsistencyValidator` 服务类：

```java
public class DemandLightingFeedbackConsistencyValidator {
    // 校验当前保存的回路反馈条件在多空间场景下是否一致
    public void validate(String projectId, Long templateId, Long sceneId,
                         String currentSpaceId,
                         List<DemandConfigLightingFuncTypeValueObject> currentFuncTypeSettings);
}
```

## 4. LightingFeedbackAdjustTask 重构 (compute-edge 模块)

### 4.1 整体重构

**文件**: `local/meos-control-compute-edge/.../task/lighting/LightingFeedbackAdjustTask.java`

当前任务仅按空间维度遍历和写入。重构后拆分为两个写入维度：

```
execute()
  ├── 1. 查询当前场景所有空间的反馈配置 (不变, 用于 illumFbCtrType)
  ├── 2. 空间维度写入: illumFbCtrType → 空间对象 (逻辑基本不变)
  ├── 3. 查询当前场景所有回路的反馈配置 (新增)
  └── 4. 回路维度写入: 4个条件信息点 → 回路对象 LTLTCC (新增)
```

### 4.2 空间维度写入 (illumFbCtrType)

保持原有逻辑，写入对象仍为空间对象：
- 解析空间的 `spaceFeedbackAdjustType`（室内=1, 室外=2, 无反馈=3）
- 增量对比：上个 5min vs 当前 5min 的场景配置变化
- 写入空间对象

### 4.3 回路维度写入 (4 个条件信息点)

新增回路维度遍历和写入逻辑：

```java
// 查询当前场景下所有反馈调节回路
Map<String, DemandConfigLightingCircuitValueObject> circuitIdAndConfig = 
    parseCircuitFeedbackAdjustSettings(nowSceneSpaceIdAndConfig);

// 增量对比：按回路 ID 对比上个 5min vs 当前
for (String circuitId : allCircuitIds) {
    boolean prevHasConfig = prevCircuitIdsByOrder.contains(circuitId);
    boolean nowHasConfig = circuitIdAndConfig.containsKey(circuitId);
    
    if (nowHasConfig) {
        // 当前有配置：写入回路条件
        Map<String, Object> orders = parseCircuitOrders(circuitIdAndConfig.get(circuitId));
        DtpTwinAutoBatchSendDTO order = new DtpTwinAutoBatchSendDTO();
        order.setObjectId(circuitId);  // 写入回路对象
        order.setData(orders);
        circuitOrders.add(order);
    } else if (prevHasConfig) {
        // 当前无配置，上个5min有：清空回路条件
        Map<String, Object> orders = parseCircuitOrders(null);
        DtpTwinAutoBatchSendDTO order = new DtpTwinAutoBatchSendDTO();
        order.setObjectId(circuitId);
        order.setData(orders);
        circuitOrders.add(order);
    }
}
```

`parseCircuitOrders()` 方法：从回路配置解析 4 个条件信息点：
```java
orders.put(InfoCodeEnum.OUTDOOR_ILLUMINATION_VALUE_TO_TURN_ON_GENERAL_LIGHTING.getValue(), underOutdoorIlluminance);
orders.put(InfoCodeEnum.FEEDBACKOF_LIGHTING_TURNON_LAST_TIME.getValue(), underDuration);
orders.put(InfoCodeEnum.OUTDOOR_ILLUMINATION_VALUE_TO_TURN_OFF_GENERAL_LIGHTING.getValue(), overtopOutdoorIlluminance);
orders.put(InfoCodeEnum.FEEDBACKOF_LIGHTING_TURNOFF_LAST_TIME.getValue(), overtopDuration);
```

### 4.4 回路配置解析

新增 `parseCircuitFeedbackAdjustSettings()` 方法，从空间配置中提取回路级反馈条件：

```java
private Map<String, DemandConfigLightingCircuitValueObject> parseCircuitFeedbackAdjustSettings(
        Map<String, DemandConfigAggregate<DemandConfigLightingValueObject>> spaceIdAndConfig) {
    Map<String, DemandConfigLightingCircuitValueObject> result = new HashMap<>();
    for (DemandConfigAggregate<DemandConfigLightingValueObject> aggregate : spaceIdAndConfig.values()) {
        DemandConfigLightingValueObject globalConfig = aggregate.getFinalConfig().getGlobalConfig();
        for (DemandConfigLightingFuncTypeValueObject funcTypeSetting : globalConfig.getGlobalSettings()) {
            if (funcTypeSetting.getSettingMode() != FEEDBACK_ADJUSTMENT) continue;
            for (DemandConfigLightingCircuitValueObject circuit : funcTypeSetting.getCircuitSettings()) {
                if (circuit.getSettingMode() == FEEDBACK_ADJUSTMENT) {
                    result.put(circuit.getCircuitId(), circuit);
                }
            }
        }
    }
    return result;
}
```

### 4.5 Redis 缓存变更

**文件**: `common/.../model/base/CachePersistenceKey.java`

- 当前：`lightingFeedbackAdjustRecord(projectId)` 存储空间 ID 集合
- 变更：新增回路级缓存 key，或改为存储回路 ID 集合

方案：新增 `lightingCircuitFeedbackAdjustRecord(projectId)` key 用于回路级记录，保留原空间级 key：
```java
static Key lightingCircuitFeedbackAdjustRecord(@NonNull String projectId) {
    return new Key(Duration.ofMinutes(9), "lighting_circuit_feedback_adjust_record", projectId);
}
```

### 4.6 DtpTwinAutoBatchSendDTO 使用

空间指令和回路指令使用相同的 `DtpTwinAutoBatchSendDTO`，区别在于 `objectId`：
- 空间指令：`objectId` = 空间 ID
- 回路指令：`objectId` = 回路 ID (LTLTCC)

两批指令分别调用 `dtpTwinClientService.autoCommandBatchSend()` 下发。

## 5. 新增信息点定义

### 5.1 InfoCodeEnum 新增

**文件**: `common/.../model/base/InfoCodeEnum.java`

```java
GENERAL_LIGHTING_FEEDBACK_ADJUSTMENT_RESULT(
    "generalLightingFeedbackAdjustmentResult", 
    "key_<新生成的UUID>"
)
```

### 5.2 i18n 新增

在 `messages_zh.properties` 和 `messages_en.properties` 中新增：
```properties
key_<UUID>=普通照明反馈调节判断结果
```

### 5.3 AutoMaticInfoCodeEnum 新增（如需）

若该信息点需要在自动指令中使用，同步在 `AutoMaticInfoCodeEnum` 中注册。

## 6. 配置预览适配

### 6.1 回路级条件展示

配置预览接口需返回回路级的自动开启/关闭条件。当前配置预览通过 `DemandConfigLightingResultDataDTO` 返回回路配置状态，需扩展返回回路级条件。

### 6.2 配置计算适配

`DemandConfigLightingComputeNowServiceImpl.putCircuitIdResults()` 需将回路级条件从配置传递到结果：

```java
// 当前: new DemandConfigLightingResultDataDTO(settingMode, null)
// 变更: new DemandConfigLightingResultDataDTO(settingMode, circuitFeedbackAdjustSettings)
```

## 7. 二开接口

### 7.1 临时需求增删改查

复用现有临时需求接口框架，适配回路级反馈条件参数。

### 7.2 空间回路查询

复用 `demandLightingStrategy.listSpacesByCircuit()` 和 `listCircuitsByInstance()` 等已有方法。

## 8. 风险与对策

| 风险 | 影响 | 对策 |
|---|---|---|
| `DemandCommandTask.feedbackAdjustmentCompute()` 仍读取空间级条件 | common-edge 不在修改范围，配置结构变更后可能读不到条件 | 空间级 `spaceFeedbackAdjustSettings` 字段保留但不写入新数据；`DemandCommandTask` 在 common-edge 部署场景下不受影响（与 compute-edge 是不同部署模式） |
| 多空间一致性校验性能 | 大量回路+空间时校验耗时 | 校验仅针对 `FEEDBACK_ADJUSTMENT` 回路，且仅比较同回路不同空间的配置，查询范围可控 |
| 回路指令写入 bdtp 失败 | 部分回路条件未更新 | 沿用现有 `dtpTwinClientService.autoCommandBatchSend()` 批量写入和 Redis 记录机制 |
| `DemandConfigLightingResultDataDTO` 结构变更 | 序列化兼容性 | 新增字段而非修改现有字段，保持向前兼容 |

## 9. 模块影响范围

| 模块 | 影响文件 | 变更类型 |
|---|---|---|
| common | `DemandConfigLightingCircuitValueObject` | 扩展字段 + 校验 |
| common | `DemandConfigLightingFeedbackAdjustDetailValueObject` | 废弃 allFeedback + 条件字段 |
| common | `DemandConfigLightingFuncTypeValueObject` | 调整 checkAndSet 逻辑 |
| common | `DemandLightingCircuitSettingDTO` | 扩展字段 |
| common | `DemandLightingTemplateSceneConfigSaveParam` | 调整字段语义 |
| common | `DemandConfigLightingResultDataDTO` | 调整条件字段 |
| common | `DemandConfigLightingComputeNowServiceImpl` | 传递回路级条件到结果 |
| common | `InfoCodeEnum` | 新增信息点 |
| common | `messages_zh.properties` / `messages_en.properties` | 新增 i18n key |
| common | `CachePersistenceKey` | 新增回路级缓存 key |
| saas | `DemandLightingServiceImpl` | 新增一致性校验 |
| saas | 新增 `DemandLightingFeedbackConsistencyValidator` | 一致性校验服务 |
| compute-edge | `LightingFeedbackAdjustTask` | 重构为空间+回路双维度写入 |
