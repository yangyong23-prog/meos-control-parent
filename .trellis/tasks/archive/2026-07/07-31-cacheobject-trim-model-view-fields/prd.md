# CacheObject剔除模型图字段存储与查询

## Goal

`cache_object.object_data` 不再存储、不再返回三个模型图字段（`bimId`、`bimLocation`、`outline`）。这三个字段的唯一存储来源收敛为 `cache_object_model_view.view_data`；前端需要模型图字段时单独调用已有接口 `POST /cacheRwd/listModelViewFieldsByObjectId`。

## 已确认决策

- **全量剔除**：包括 SPACE 与通风设备类（FFSCMF/FFSCAF/ACVTSF）的 `bimId`/`bimLocation`。（2026-07-31 用户确认）
- **通风值班首页不做读取侧合并**：`POST /instance/listDashboardSpaces/VENTILATION` 响应将不再携带空间与设备的 `bimId`/`bimLocation`，前端 3D 地图定位改为单独调用 `/cacheRwd/listModelViewFieldsByObjectId`。前端切换由用户侧安排，本任务只改后端。
- 末端、照明 READY 路径本就不从 `cache_object` 请求这三个字段，行为不变。

## Requirements

- R1 存储侧：`CacheObjectExtensionFieldConfig.OBJECT_EXTENSION_FIELDS` 所有 classCode 条目剔除 `bimId`/`bimLocation`/`outline`；剔除后仅剩这三个字段的 19 个设备类条目整条删除（未配置返回空集合是已有合法语义）。
- R2 查询侧：所有从 `cache_object` 读取的请求字段集合不再包含这三个字段，涉及：
  - `CacheRwdServiceImpl.WHITELIST_SPACE_FIELDS`（`/cacheRwd/whitelist/query` CUSTOM 分支）
  - `RunningMonitorAirTerminalStrategy.CACHE_SPACE_FIELDS`（白名单空间缓存分支）
  - `RunningMonitorVentilationStrategy.getVentilationDtpRwdSpaceVOSFromNewCache` 的空间字段集与设备字段集（设备当前为 `null` 全字段恢复，需改为显式窄字段）
  - `AbstractVentilationIotService.listEquipments` READY 分支（当前 `null` 全字段恢复，改为显式窄字段）
- R3 写入路径不变：`listRwdQueryFields`（基础字段 + 对象字段 + 模型视图字段的并集）继续用于 RWD 查询与缓存写入，`cache_object_model_view.view_data` 照常写入三个模型图字段。
- R4 响应兼容边界：除"决策"中两处既定差异外，所有接口响应字段不变。
- R5 不做数据迁移：读取投影为"存储白名单 ∩ 请求字段"，存量 object_data 中的旧字段不会透出；发布后通过常规缓存重建（通风/末端/照明）物理清除存量冗余。

## Acceptance Criteria

- [ ] 全仓库检索确认：不存在从 `cache_object` 请求 `bimId`/`bimLocation`/`outline` 的读取点（写入路径的 RWD 查询并集除外）。
- [ ] `common` 模块编译通过；`CacheObjectRecordConverterTest`、`RunningMonitorLightingStrategyTest`、`RunningMonitorAirTerminalWhitelistRoutingTest` 等既有测试不回归。
- [ ] 缓存重建后，SPACE 的 object_data 仅含 `roomFuncType`、`replacedByNatLighting`；ACVTSF 仅含 `funcType`；LIGHTING_CIRCUIT 仅含 `serviceAreaType`/`funcType`/`lampFuncType`；仅配置过 BIM 字段的设备类 object_data 为 `{}`。
- [ ] `/cacheRwd/listModelViewFieldsByObjectId` 对 SPACE/设备返回 `bimId`/`bimLocation`（SPACE 含 `outline`）行为不变。

## 影响范围（基于代码调用链）

| 接口 | 变化 | 消费方影响 |
|---|---|---|
| `POST /instance/listDashboardSpaces/VENTILATION` | 空间与设备不再返回 `bimId`/`bimLocation` | 通风值班首页 3D 地图的空间标记、设备图标、空间-设备飞线依赖这两个字段；前端必须切换到 `/cacheRwd/listModelViewFieldsByObjectId`，切换完成前 READY 环境下地图标记缺失。**发布顺序需前后端协同** |
| `POST /cacheRwd/whitelist/query`（CUSTOM 白名单分支） | 响应 objects 减少 `bimId`/`bimLocation`/`outline` | 白名单配置页仅使用对象 id 与数量，已核实无消费，无感 |
| 末端/照明值班首页及其余 READY 接口 | 无变化 | 无 |
| `POST /cacheRwd/listModelViewFieldsByObjectId` | 无变化 | 成为模型图字段唯一获取入口 |

## Notes

- 风险：投影是静默交集，若存在遗漏的请求方，字段将静默为 null 不报错；以全量检索 + 既有测试兜底。
- 耦合记录：前端 `groupHoverPanel` 的 HOVER 模板调用携带 `bimLocation`，当前因 HOVER 不在缓存许可矩阵（`InfoCodeTemplateTypeEnum.allowCache`）而实时查 RWD；若未来 HOVER 纳入矩阵，它将依赖模型视图接口而非 `cache_object`。
- `listWhitelistSpaces` 缓存分支当前所有调用方均 `setMeosCache(false)`，生产不可达；本次仍同步修剪其字段常量保持契约一致。
