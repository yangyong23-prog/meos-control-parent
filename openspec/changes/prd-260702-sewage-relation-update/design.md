## Context

当前污废水模块中，潜污泵（WSSSTSP）与餐饮除油成套设备（WSSTOR）、污废水一体式提升机组（WSSTLU）之间的设备关系使用 `MechLocation/Inside`（设备所在设备）。根据 PRD-260702，这一语义应修正为 `MechSubset/Eq2Eq`（设备下的设备），方向由“潜污泵指向父设备”反转为“父设备指向潜污泵”。

线上存在仅建立了旧关系的老数据项目，因此列表页和详情页不能一刀切地使用新关系，必须在新关系无数据时自动回退到旧关系。

相关关键代码：

- 旧关系枚举：`DtpRwdRelationTypeEnum.EQ_LOCATION_IN_EQ`（`MechLocation/Inside`）
- 新关系枚举：`DtpRwdRelationTypeEnum.EQ_IN_EQ`（`MechSubset/Eq2Eq`）
- info code 关联查询上下文：[`InfoCodeRwdObjectDetailByTemplateContext`](file:///D:/renew2/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/context/InfoCodeRwdObjectDetailByTemplateContext.java)
- Java 侧旧关系查询入口：[`DtpRwdClientService.listLocationInsideByEquipmentIds`](file:///D:/renew2/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/outside/feign/service/DtpRwdClientService.java)
- Java 侧直接调用点：[`RunningMonitorSewageStrategy`](file:///D:/renew2/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/structure/common/strategy/impl/RunningMonitorSewageStrategy.java)、[`WaterSupplyHomePageCardDataHandler`](file:///D:/renew2/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/service/duty/strategy/WaterSupplyHomePageCardDataHandler.java)
- 受影响消费接口：
  - [`/dashboardGridChart/queryEquipment`](file:///D:/renew2/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/controller/dashboard/DashboardGridChartController.java)（楼层卡片右侧设备统计，底层走 `RunningMonitorSewageStrategy.listEquipments`）
  - [`/equipment/infoCodeConfig/listInfoCodeConfigsByObject`](file:///D:/renew2/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/controller/infocodeconfig/InfoCodeConfigController.java)（设备详情页信息点，RWD_OBJECT_DETAIL 走 `InfoCodeRwdObjectDetailByTemplateContext.fillObjects()`）
- 配置模板：`【SEWAGE-TABLE_LIST】设备列表模版.xlsx`、`【SEWAGE-DETAIL】设备详情页模版.xlsx`

## Goals / Non-Goals

**Goals：**

- 将 WSSTOR、WSSTLU 与 WSSSTSP 的关系查询从 `MechLocation/Inside` 迁移到 `MechSubset/Eq2Eq`。
- 列表页、详情页在新关系无数据时自动回退到旧关系，避免老数据项目关联设备为空。
- 运行监测、值班首页以及 `/dashboardGridChart/queryEquipment` 的 Java 调用链路同步支持新旧关系 fallback。
- `/equipment/infoCodeConfig/listInfoCodeConfigsByObject` 返回的 RWD_OBJECT_DETAIL 关系信息点通过 info code 框架 fallback 自动兼容。
- 不改动未涉及的 WSSTCP、WSSTOS 关系配置。
- 不改动现有 info code 框架的单查询逻辑，仅新增 fallback 能力。

**Non-Goals：**

- 系统图（system graph）与楼层卡片不在本次改造范围内。
- 不为 `source` 查询增加 fallback。
- 不删除或修改 `listLocationInsideByEquipmentIds` 旧方法。
- 不迁移已有项目的历史关系数据（仅通过查询 fallback 兼容）。

## Decisions

### 1. 整批查询 fallback（而非按 rootObjectId 粒度）

**选择**：在 `InfoCodeRwdObjectDetailByTemplateContext.fillObjects()` 中，主查询只要返回任意结果，就全部使用主查询结果；主查询为空时才尝试 fallback。

**理由**：
- 实现简单，只需在主查询外层包一轮循环，现有 `fillObjects()` 流程几乎不变。
- 用户已明确接受该语义：同一批设备中若部分已迁移、部分未迁移，未迁移设备会暂时丢失关系数据。

**替代方案**：按单个 `rootObjectId` 粒度 fallback。可最大程度兼容混合迁移场景，但实现更复杂（需要维护 pending root IDs、按缺失 ID 分批查询、结果去重）。当前用户不接受这种复杂度。

### 2. 新增 `fallbackQueryParams` 字段，不改动 `queryParam`

**选择**：在 `InfoCodeTakeValueLogicRwdObjectDetailModel` 中新增 `List<DtpRwdObjectQueryParam> fallbackQueryParams`，保持现有 `queryParam` 必填且语义不变。

**理由**：
- 未配置 `fallbackQueryParams` 时，代码路径与现在完全一致，避免回归。
- 配置表达直观：主查询 + 兜底查询列表，符合 Excel 配置人员的理解。

### 3. Java 侧新增专用 fallback 方法，不扩展通用 `listObject`

**选择**：在 `DtpRwdClientService` 新增 `listSubmersibleSewagePumpByEquipmentIds` 方法，内部先查新关系、无结果再查旧关系。原 `listLocationInsideByEquipmentIds` 保留。

**理由**：
- 该 fallback 语义是针对污水设备关系的业务需求，不适合下沉到通用 `listObject`。
- 保持旧方法不变，符合“尽量少改旧逻辑”的约束。

### 4. 系统图 / 楼层卡片本次不改

**选择**：系统图由 DTP 关系图引擎远程生成，楼层卡片依赖系统图节点 ID，均不直接查关系，因此不纳入 fallback 范围。

**理由**：
- 当前 Java 代码里没有系统图的关系类型硬编码。
- 若 PRD 后续要求系统图也反映新关系，需要单独评估 DTP 引擎模板配置，不在本次代码改造范围内。

### 5. 新关系查询方向修正——relationFrom/relationTo 与 objFrom/objTo 需翻转

**选择**：新关系 `MechSubset/Eq2Eq` 的方向与旧关系 `MechLocation/Inside` 恰好相反，因此查询同一目标时，`relationFrom`/`relationTo` 和 `objFrom`/`objTo` 必须全部互换。初始实现错误地沿用了旧关系的 `setRelationFrom + setObjTo` 模式，导致新关系查询方向错误。

**方向映射**：

| 关系 | from→to | 查泵(FROM/TO侧) | 查父(FROM/TO侧) |
|---|---|---|---|
| 旧 `MechLocation/Inside` | 泵(FROM)→父(TO) | `relationFrom + objTo(父ID)` | `relationTo + objFrom(泵ID)` |
| 新 `MechSubset/Eq2Eq` | 父(FROM)→泵(TO) | `relationTo + objFrom(父ID)` | `relationFrom + objTo(泵ID)` |

**受影响位置**：
- Java 代码：`DtpRwdClientServiceImpl.listSubmersibleSewagePumpByEquipmentIds` 新关系主查询（第 899-900 行），由 `setRelationFrom + setObjTo` 改为 `setRelationTo + setObjFrom`
- Excel `【SEWAGE-TABLE_LIST】设备列表模版.xlsx`：WSSTOR/WSSTLU 主查询 JSON 由 `relationFrom + objTo` 改为 `relationTo + objFrom`；WSSSTSP 反向主查询由 `relationTo + objFrom` 改为 `relationFrom + objTo`
- Excel `【SEWAGE-DETAIL】设备详情页模版.xlsx`：WSSTOR/WSSTLU 主查询 JSON 同上翻转
- fallback 查询不变（旧关系方向本就正确）

**理由**：
- 根据 `.trellis/spec/backend/rwd-relation-direction.md` 规范，`relationFrom` 必须搭配 `objTo`，`relationTo` 必须搭配 `objFrom`。新关系方向反转后，查泵从原来的 FROM 侧变为 TO 侧，因此 API 调用方式必须对应翻转。
- spec 中已正确描述方向（`objFrom` = parent equipment），但实现未遵循，需同步修正。

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| 同一批查询中部分设备已迁移、部分未迁移，未迁移设备的关系数据会丢失 | 用户已明确接受该风险；项目迁移时建议按项目维度整体切换，避免混合批次 |
| 新增 `fallbackQueryParams` 后，配置人员可能误配空查询或无效关系 | `InfoCodeConfigImportContext` 增加校验，确保每个 fallback 项包含有效查询条件 |
| 老数据项目长期同时存在新旧两套关系，导致展示不一致 | 通过文档明确“新关系优先”策略；后续可通过数据治理脚本统一清理旧关系 |
| 每批次多一次 fallback 远程调用，性能开销增加 | fallback 仅在主查询为空时触发；且查询仍按批量 ID 发起，不会 N+1 |

## Migration Plan

1. **代码发布**：
   - 部署 info code 框架改造、`DtpRwdClientService` 新方法、两个 Java 调用点修改、配置导入校验。
2. **Excel 配置更新**：
   - 修改 `【SEWAGE-TABLE_LIST】设备列表模版.xlsx` 与 `【SEWAGE-DETAIL】设备详情页模版.xlsx` 中 WSSTOR、WSSTLU、WSSSTSP 反向关联相关配置，重新导入。
3. **关系配置表更新**（如需要）：
   - 由业务/数据团队确认是否需要在 `rc_object_relation_cfg` 中新增 `MechSubset/Eq2Eq` 关系配置项，以支持新关系建立。本次代码改造不强制要求。
4. **验证**：
   - 新项目：仅建立新关系，验证列表/详情/运行监测/值班首页正常展示。
   - 老项目：仅建立旧关系，验证页面通过 fallback 仍能展示潜污泵。
5. **回滚**：
   - 回滚代码后，已配置的 `fallbackQueryParams` 字段不会被解析（若旧代码忽略未知字段），但为安全起见，可先回滚 Excel 配置到旧关系，再回滚代码。

## Open Questions

- `rc_object_relation_cfg` 中是否需要新增 WSSTOR↔WSSSTSP、WSSTLU↔WSSSTSP 的 `MechSubset/Eq2Eq` 配置行？这决定了新关系数据能否在系统中建立，但不在本次代码改造范围内，需业务确认。
