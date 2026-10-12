# Implement — CacheObject剔除模型图字段存储与查询

按序执行；每步完成后进入下一步。依据：`prd.md` → `design.md`。

## 执行清单

- [x] 1. `CacheObjectExtensionFieldConfig`：修剪 `OBJECT_EXTENSION_FIELDS`（SPACE/AC_SPACE/ACVTSF/LIGHTING_CIRCUIT 收窄，19 个仅 BIM 设备类条目删除）；新增 `listObjectQueryFields(classCode)`；更新类与 Map 的 Javadoc 契约说明。
- [x] 2. `RunningMonitorVentilationStrategy.getVentilationDtpRwdSpaceVOSFromNewCache`：空间字段集改为 `listObjectQueryFields(SPACE) + name`（删除排除 outline 的逻辑与注释）；设备读取由 `null` 改为三类 `listObjectQueryFields` 并集 + `name`。
- [x] 3. `AbstractVentilationIotService.listEquipments` READY 分支：`null` → `{id, classCode, FUNC_TYPE}`。（trellis-check 阶段将内联集合重构为命名变量并复验通过。）
- [x] 4. `CacheRwdServiceImpl.WHITELIST_SPACE_FIELDS`：删除 `bimId`/`bimLocation`/`outline`。
- [x] 5. `RunningMonitorAirTerminalStrategy.CACHE_SPACE_FIELDS`：删除 `bimId`/`bimLocation`/`outline`。
- [x] 6. 静态验证：检索 `bimId`/`bimLocation`/`outline` 在 `cache_object` 读取路径（listByCacheKey/listByObjectIds/mapByCacheKeys 调用点及字段常量）的残留引用，应为零（写入路径 `listRwdQueryFields` 调用点除外）。
- [x] 7. 编译与单测：
  - `mvn -pl common compile -q`
  - `mvn -pl common test -Dtest=CacheObjectRecordConverterTest,RunningMonitorLightingStrategyTest,RunningMonitorAirTerminalWhitelistRoutingTest,InfoCodeTemplateTypeEnumCacheTest -q`
- [x] 8. 质量检查：按 `trellis-check` 执行规格符合性、跨层数据流与一致性检查。（发现并已修复步骤 3 的字段集遗漏；修复后 `mvn -pl common compile` 与 `CacheObjectRecordConverterTest` 复跑通过。）

## 审查关口

- 步骤 1 完成后：确认 `OBJECT_EXTENSION_FIELDS` 与 `MODEL_VIEW_FIELDS` 无交集（模型图字段只在 MODEL_VIEW_FIELDS）。
- 步骤 6 完成后：确认无静默缺失遗漏点。
- 步骤 7 失败：按新契约修正断言或回查字段集，禁止为通过测试放宽白名单语义。

## 回滚点

- 全部变更为配置常量与字段集合修改，`git revert` 单次提交即可整体回滚；无 DDL、无数据迁移，回滚无数据残留风险。

## 发布后动作（不在本任务代码范围）

- 触发 VENTILATION / AIR_TERMINAL / LIGHTING 缓存重建，物理清除存量 object_data 中的模型图字段。
- 同步前端：通风值班首页 3D 地图模型图字段来源切换至 `POST /cacheRwd/listModelViewFieldsByObjectId`。
