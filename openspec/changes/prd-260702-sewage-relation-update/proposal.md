## Why

根据 PRD-260702，潜污泵（WSSSTSP）与餐饮除油成套设备（WSSTOR）、污废水一体式提升机组（WSSTLU）之间的设备关系需要从“设备所在设备”（MechLocation/Inside）修正为“设备下的设备”（MechSubset/Eq2Eq），关系方向由“潜污泵指向父设备”反转为“父设备指向潜污泵”。由于线上存在仅建立了旧关系的老数据项目，列表页和详情页必须在新关系无结果时自动回退到旧关系，避免页面关联设备为空。

## What Changes

- **info code 框架能力扩展**：为 `RWD_OBJECT_DETAIL` 类型的取值逻辑模型增加 `fallbackQueryParams`，并在 `InfoCodeRwdObjectDetailByTemplateContext.fillObjects()` 中实现整批查询 fallback：主查询（新关系）返回任意结果时全部使用新关系；否则依次尝试 fallback（旧关系）。
- **Java 侧关系查询兼容**：在 `DtpRwdClientService` 中新增 `listSubmersibleSewagePumpByEquipmentIds` 方法，内部同样按“新关系优先、无结果则回退旧关系”执行；`RunningMonitorSewageStrategy` 与 `WaterSupplyHomePageCardDataHandler` 改用该方法，旧方法 `listLocationInsideByEquipmentIds` 保留不动。该链路同时覆盖 `/dashboardGridChart/queryEquipment` 返回的 `SewageEquipVO.relEquipment`。
- **Excel 配置更新**：调整 `【SEWAGE-TABLE_LIST】设备列表模版.xlsx` 与 `【SEWAGE-DETAIL】设备详情页模版.xlsx` 中 WSSTOR、WSSTLU 以及 WSSSTSP 反向关联这两个类的关系查询配置，使其主查询使用 `MechSubset/Eq2Eq` 并配置 `MechLocation/Inside` 作为 fallback。
- **配置导入校验**：`InfoCodeConfigImportContext` 增加对 `fallbackQueryParams` 的合法性校验。
- **关系方向修正**：新关系 `MechSubset/Eq2Eq` 方向与旧关系 `MechLocation/Inside` 恰好相反，初始实现错误地沿用了旧关系的 `relationFrom + objTo` 模式。需将 Java 代码与 Excel 配置中新关系主查询的 `relationFrom/relationTo` 与 `objFrom/objTo` 全部翻转：父设备查泵由 `relationFrom + objTo` 改为 `relationTo + objFrom`，泵查父设备由 `relationTo + objFrom` 改为 `relationFrom + objTo`。fallback 查询不变。
- **系统图 / 楼层卡片**：系统图生成及非 `dashboardGridChart/queryEquipment` 链路的楼层卡片统计本次不变更；通过 `/dashboardGridChart/queryEquipment` 取数的右侧设备统计随 `RunningMonitorSewageStrategy` 一并兼容。

## Capabilities

### New Capabilities

- `info-code-relation-fallback`：info code 框架的 `RWD_OBJECT_DETAIL` 支持配置多个 fallback 查询参数，按整批查询策略自动回退，兼容新旧设备关系数据。
- `sewage-equipment-relation`：污废水模块中 WSSTOR/WSSTLU 与 WSSSTSP 的设备关系从 MechLocation/Inside 迁移到 MechSubset/Eq2Eq，并在列表/详情页、运行监测、值班首页及 `/dashboardGridChart/queryEquipment` 通过 fallback 机制兼容老数据。

### Modified Capabilities

- 无（现有 spec 目录 `openspec/specs/cache`、`openspec/specs/code-style`、`openspec/specs/logging` 与本变更无关）。

## Impact

- **影响文件**：
  - `common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/model/tackvalue/InfoCodeTakeValueLogicRwdObjectDetailModel.java`
  - `common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/context/InfoCodeRwdObjectDetailByTemplateContext.java`
  - `common/src/main/java/com/persagy/apccontrol/basic/model/context/infocodeconfig/excel/InfoCodeConfigImportContext.java`
  - `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/service/DtpRwdClientService.java`
  - `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/service/impl/DtpRwdClientServiceImpl.java`
  - `common/src/main/java/com/persagy/apccontrol/basic/structure/common/strategy/impl/RunningMonitorSewageStrategy.java`
  - `common/src/main/java/com/persagy/apccontrol/basic/service/duty/strategy/WaterSupplyHomePageCardDataHandler.java`
  - `common/src/main/java/com/persagy/apccontrol/basic/controller/dashboard/DashboardGridChartController.java`（受影响消费端，无代码改动）
  - `common/src/main/java/com/persagy/apccontrol/basic/controller/infocodeconfig/InfoCodeConfigController.java`（受影响消费端，无代码改动）
  - `【SEWAGE-TABLE_LIST】设备列表模版.xlsx`
  - `【SEWAGE-DETAIL】设备详情页模版.xlsx`
- **受影响消费端**：`/dashboardGridChart/queryEquipment`（楼层卡片右侧设备统计）、`/equipment/infoCodeConfig/listInfoCodeConfigsByObject`（设备详情页 RWD_OBJECT_DETAIL 关系信息点）。
- **不影响**：系统图生成、`dashboardGridChart/queryEquipment` 与 `RunningMonitorSewageStrategy` 以外的楼层卡片统计、运行监测以外的其他策略、`rc_object_relation_cfg` 之外的代码逻辑。
- **兼容性**：未配置 `fallbackQueryParams` 的现有 `RWD_OBJECT_DETAIL` 配置行为保持不变。
