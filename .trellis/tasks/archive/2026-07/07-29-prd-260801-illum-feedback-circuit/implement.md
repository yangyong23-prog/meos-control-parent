# 执行计划：PRD-26.0801 照度反馈条件按回路维度配置

## 执行顺序

按依赖关系从底层到上层、从配置到下发执行。

---

### Step 1: 扩展回路配置值对象

**目标**: `DemandConfigLightingCircuitValueObject` 新增 4 个反馈条件字段 + 校验逻辑

**文件**:
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/model/valueobject/lighting/children/DemandConfigLightingCircuitValueObject.java`
- `common/src/main/java/com/persagy/apccontrol/basic/model/dto/demand/lighting/DemandLightingCircuitSettingDTO.java`（同步扩展）

**变更内容**:
1. 新增 `underOutdoorIlluminance`(BigDecimal)、`underDuration`(Integer)、`overtopOutdoorIlluminance`(BigDecimal)、`overtopDuration`(Integer)
2. 新增 `checkAndSet()` 方法：
   - `FEEDBACK_ADJUSTMENT` 时校验条件最少配一项、阈值大小关系
   - `OPEN`/`CLOSE` 时清空 4 个条件字段
3. DTO 同步新增字段

**验证**: 编译通过，单元测试覆盖校验逻辑

---

### Step 2: 调整空间级反馈配置结构

**目标**: 废弃 `allFeedback` 和空间级条件字段，反馈方式保留在空间级

**文件**:
- `common/.../children/DemandConfigLightingFeedbackAdjustDetailValueObject.java`
- `common/.../children/DemandConfigLightingFuncTypeValueObject.java`

**变更内容**:
1. `DemandConfigLightingFeedbackAdjustDetailValueObject`: 废弃 `allFeedback` + 4 个条件字段（保留类定义避免序列化断裂，或标注 `@Deprecated`）
2. `DemandConfigLightingFuncTypeValueObject.checkAndSet()`:
   - `FEEDBACK_ADJUSTMENT` case: 移除 `allFeedback` 判断，改为遍历 `circuitSettings` 调用回路级 `checkAndSet()`
   - 移除 `spaceFeedbackAdjustSettings` 的非空校验

**验证**: 编译通过，现有测试不受破坏

---

### Step 3: 调整保存参数和计算结果

**目标**: 保存参数和计算结果适配回路级条件

**文件**:
- `common/.../model/param/demand/lighting/DemandLightingTemplateSceneConfigSaveParam.java`
- `common/.../model/dto/demand/lighting/DemandConfigLightingResultDataDTO.java`
- `common/.../service/demand/lighting/DemandConfigLightingComputeNowServiceImpl.java`

**变更内容**:
1. `DemandLightingTemplateSceneConfigSaveParam`: `spaceFeedbackAdjustSettings` 字段语义调整为仅携带反馈方式
2. `DemandConfigLightingResultDataDTO`: `feedbackAdjustSettings` 改为携带回路级条件，或新增 `circuitFeedbackAdjustSettings` 字段
3. `DemandConfigLightingComputeNowServiceImpl.putCircuitIdResults()`: 将回路级条件从配置传递到结果（当前传 `null`，改为传回路条件）

**验证**: 编译通过，配置计算流程正常

---

### Step 4: 新增"判断结果"信息点

**目标**: 在 `InfoCodeEnum` 中定义新信息点

**文件**:
- `common/src/main/java/com/persagy/apccontrol/basic/model/base/InfoCodeEnum.java`
- `common/src/main/resources/i18n/messages_zh.properties`
- `common/src/main/resources/i18n/messages_en.properties`
- `common/.../model/enumtype/AutoMaticInfoCodeEnum.java`（如需）

**变更内容**:
1. 生成新 i18n key UUID
2. `InfoCodeEnum` 新增 `GENERAL_LIGHTING_FEEDBACK_ADJUSTMENT_RESULT`
3. i18n 文件新增中英文定义
4. `AutoMaticInfoCodeEnum` 注册（如需参与自动指令）

**验证**: 编译通过，i18n key 可正确解析

---

### Step 5: 新增多空间回路一致性校验

**目标**: 保存时校验同一回路在多空间中的反馈条件一致性

**文件**:
- 新增 `saas/meos-control-common/.../service/demand/validator/DemandLightingFeedbackConsistencyValidator.java`
- `saas/meos-control-common/.../service/demand/impl/DemandLightingServiceImpl.java`

**变更内容**:
1. 新增 `DemandLightingFeedbackConsistencyValidator`:
   - `validate()` 方法：提取当前空间的反馈调节回路 → 查询同模板同场景其他空间配置 → 比较同回路条件一致性
   - 不一致时抛出异常，返回冲突回路名称和各空间配置详情
2. `DemandLightingServiceImpl.editSceneObjectSettings()`:
   - 在配置校验后、持久化前调用 `validator.validate()`
   - 并发保存冲突：事务内查询最新数据再次校验

**验证**: 编译通过，单元测试覆盖一致/不一致场景

---

### Step 6: 重构 LightingFeedbackAdjustTask

**目标**: 从空间维度改为空间+回路双维度写入

**文件**:
- `local/meos-control-compute-edge/.../task/lighting/LightingFeedbackAdjustTask.java`
- `common/.../model/base/CachePersistenceKey.java`

**变更内容**:
1. `CachePersistenceKey`: 新增 `lightingCircuitFeedbackAdjustRecord(projectId)` 回路级缓存 key
2. `LightingFeedbackAdjustTask.execute()` 重构:
   - 保留空间维度 `illumFbCtrType` 写入逻辑
   - 新增回路维度条件信息点写入逻辑
   - 新增 `parseCircuitFeedbackAdjustSettings()` 从空间配置中提取回路级条件
   - 新增 `parseCircuitOrders()` 解析回路条件为 4 个信息点指令
   - 回路指令 `objectId` = 回路 ID (LTLTCC)
   - 回路级 Redis 缓存记录
3. 增量对比逻辑：按回路 ID 对比上个 5min vs 当前

**验证**: 编译通过，定时任务执行无异常，空间和回路指令正确下发

---

### Step 7: 配置预览适配

**目标**: 配置预览返回回路级反馈条件

**文件**:
- 相关配置查询接口（根据实际代码定位）

**变更内容**:
1. 配置预览查询返回回路级 `underOutdoorIlluminance`/`underDuration`/`overtopOutdoorIlluminance`/`overtopDuration`
2. `settingMode = FEEDBACK_ADJUSTMENT` 的回路展示自动开启/关闭条件

**验证**: 配置预览接口正确返回回路级条件

---

### Step 8: 二开接口

**目标**: 临时需求增删改查 + 空间回路查询接口

**文件**:
- 临时需求接口（根据实际代码定位）
- 空间回路查询接口

**变更内容**:
1. 临时需求接口适配回路级反馈条件参数
2. 空间回路查询复用已有 `demandLightingStrategy` 方法

**验证**: 接口可用，参数正确传递

---

### Step 9: 整体验证

**验证项**:
1. 编译通过（common + saas + compute-edge 模块）
2. 配置保存 → 同步计算 → 回路结果正确
3. 多空间一致性校验正确触发
4. `LightingFeedbackAdjustTask` 5min 定时执行，空间和回路指令正确下发
5. Redis 缓存记录正确（空间级 + 回路级）
6. 配置预览正确展示回路级条件

---

## 回滚点

- Step 1-2 完成后：配置模型变更，可独立回滚
- Step 3 完成后：保存和计算适配，需与 Step 1-2 一起回滚
- Step 4 完成后：信息点定义，可独立保留
- Step 5 完成后：一致性校验，可独立回滚（移除校验调用）
- Step 6 完成后：下发任务重构，需与 Step 1-3 一起回滚
- Step 7-8 完成后：预览和二开接口，可独立回滚
