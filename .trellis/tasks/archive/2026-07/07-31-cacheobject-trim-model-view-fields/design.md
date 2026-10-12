# Design — CacheObject剔除模型图字段存储与查询

## 契约变化

| 配置/方法 | 变更前 | 变更后 |
|---|---|---|
| `OBJECT_EXTENSION_FIELDS`（object_data 存储白名单） | 业务字段 + 模型图字段混存 | 只保留业务字段；模型图字段唯一来源为 `MODEL_VIEW_FIELDS` |
| `MODEL_VIEW_FIELDS`（view_data 存储白名单） | 不变 | 不变 |
| `listRwdQueryFields(classCode)` | 基础 + 对象 + 模型视图并集，供写入路径 RWD 查询 | 不变 |
| `listObjectQueryFields(classCode)`（新增） | 无 | 基础字段 + 对象扩展字段，供 `cache_object` 读取路径组装字段，防止读取方手写并集把模型图字段带回 |

## 文件级修改

### 1. `CacheObjectExtensionFieldConfig`（projection 包）

- `OBJECT_EXTENSION_FIELDS` 修剪：
  - `SPACE` → `{roomFuncType, REPLACED_BY_NAT_LIGHTING}`
  - `AC_SPACE` → `{roomFuncType}`
  - `FLOOR` → `{floorSequenceId}`（不变）
  - `ACVTSF` → `{FUNC_TYPE}`
  - `LIGHTING_CIRCUIT` → `{serviceAreaType, funcType, lampFuncType}`
  - 整条删除（剔除后仅剩 BIM 字段的 19 个设备类）：ACATDH、ACATAH、ACATFC、ACVRVI、ACCCWP、ACATFA、ACATVA、ACATFU、ACATRA、ACVTAC、ACAHG_GROUP、ACATHT、ACVVER、ACVVEA、FFSCAF、CFCSEW、ACVVEJ、LIGHTING_CONTROLLER、WEFCDD
- 新增 `public static Set<String> listObjectQueryFields(String classCode)`：返回 `RWD_QUERY_BASE_FIELDS ∪ getObjectFields(classCode)`，深度不可变。
- 更新类 Javadoc 与 Map 注释：明确"模型图字段（bimId/bimLocation/outline）只进模型视图 JSON，禁止进入对象扩展 JSON"的契约。
- 附带收益：只含基础字段的请求不再命中 `containsObjectExtensionField`，SQL 不再 SELECT `object_data` 列（如 `AIR_TERMINAL_CONTROL_SPACE_FIELDS`、照明关系楼层/控制对象查询）。

### 2. `RunningMonitorVentilationStrategy.getVentilationDtpRwdSpaceVOSFromNewCache`

- 空间字段集：`listRwdQueryFields(SPACE) + name - outline` → `listObjectQueryFields(SPACE) + name`（并集本就不含模型图字段，删除显式排除 outline 的注释与代码）。
- 设备字段集：`null`（全字段恢复）→ `listObjectQueryFields(FFSCMF) ∪ listObjectQueryFields(FFSCAF) ∪ listObjectQueryFields(ACVTSF) + name`。
- 响应差异：空间/设备失去 `bimId`/`bimLocation`，其余字段与今日 READY 行为完全一致（`name` 本就无存储列，静默缺失为现状）。

### 3. `AbstractVentilationIotService.listEquipments`（READY 分支）

- `null` → 显式 `{id, classCode, FUNC_TYPE}`。唯一消费者 `queryEquipGroupId()` 只需 `classCode`/`id` 分组与 ACVTSF 的 `funcType`（经 `undefinedProperties` 读取）。
- 未 READY 的 RWD 分支（`getDtpRwdEquipmentDTOS`）不变。

### 4. `CacheRwdServiceImpl.WHITELIST_SPACE_FIELDS`

- 删除 `bimId`、`bimLocation`、`outline`，保留 `{id, projectId, localName, classCode, objType, buildingId, floorId, roomFuncType, monitoringSurveillanceStatus}`。

### 5. `RunningMonitorAirTerminalStrategy.CACHE_SPACE_FIELDS`

- 删除 `bimId`、`bimLocation`、`outline`，保留 `{id, projectId, localName, classCode, objType, buildingId, floorId, roomFuncType, monitoringSurveillanceStatus}`。

## 明确不修改

- `MODEL_VIEW_FIELDS`、`listRwdQueryFields`、全部写入路径（`CacheRwd*Strategy`、`InfoCodeTemplateTypeModel`、`LightingCacheObjectRelationService`、`AirTerminalServiceImpl.listFullSpaceEquipmentForCacheBuild` 等 RWD 查询并集）。
- `CacheObjectExtensionProjector` / `CacheObjectRecordConverter` / `CacheObjectModelViewRecordConverter`（投影与重建逻辑不变，白名单驱动）。
- 前端代码；`/cacheRwd/listModelViewFieldsByObjectId` 接口。
- 无 DDL、无数据迁移脚本。

## 数据兼容与发布

- 存量 object_data 行仍含旧字段：读取投影按新白名单交集，不会透出；无需迁移。
- 发布后依次触发 VENTILATION、AIR_TERMINAL、LIGHTING 缓存重建（XXL-JOB 或 DTX 事件），object_data 物理清除模型图字段。
- 前端切换通风 3D 地图字段来源为先决协同项：前端未完成切换前发布本变更，READY 环境下通风地图标记缺失（用户已确认接受该协同方式）。

## 风险与对策

| 风险 | 对策 |
|---|---|
| 遗漏请求方导致字段静默缺失 | 全仓库检索 `bimId`/`bimLocation`/`outline` 在 cache_object 读取点的引用；既有单测与集成测试兜底 |
| 后续维护者把模型图字段加回对象白名单 | 类 Javadoc 写明契约；`getObjectFields` 是读取投影唯一入口，评审时聚焦该 Map |
| 通风设备 `funcType` 丢失影响送/排风机统计 | 设备字段集显式包含 `FUNC_TYPE`；ACVTSF 对象白名单保留 `FUNC_TYPE` |

## Test Plan

- `mvn -pl common compile` 编译通过。
- 单测：`CacheObjectRecordConverterTest`、`RunningMonitorLightingStrategyTest`、`RunningMonitorAirTerminalWhitelistRoutingTest`、`InfoCodeTemplateTypeEnumCacheTest` 不回归（均不依赖被修剪字段，预期无需改动；若断言失败按新契约修正）。
- 静态验证：检索确认 cache_object 读取点不再引用三个模型图字段。
- 数据库型集成测试（`CacheObjectModelMapperIntegrationTest`）与实机重建验证由主会话串行执行，不与浏览器验证并发。
