## Context

通风（VENTILATION）模块已完成新缓存对象模型升级，采用 `cache_object` + `cache_object_rel` 双表存储，读取侧通过 `cacheObjectModelStatusService.isReady()` 智能切换新旧路径。当前管理范围仅覆盖风机类设备（FFSCAF/ACVTSF/FFSCMF），所有设备通过 MechForArch/Eq2Sp（设备服务于空间）关系关联空间。

PRD-290902 要求扩大管理范围，引入窗类设备（CFCSWN/CFCSEW）通过 MechInArch/Eq2Sp（设备所在空间）关系纳入穹顶空间，同时废除原有 SFT-01.05.00 剔除规则。这涉及设备查询、缓存构建/读取、空间并集、设备分类、首页统计等 7 个文件 15 处改动。

## Goals / Non-Goals

**Goals:**
- 窗类设备（CFCSWN/CFCSEW）通过 EQUIPMENT_IN_SPACE 关系纳入穹顶通风感知
- 废除 SFT-01.05.00 剔除规则，重新纳管服务挑空空间的风机
- 空间范围 = 风机服务空间 ∪ 窗所在穹顶空间
- 缓存写入/读取路径支持双关系类型（EQUIPMENT_SERVICE_SPACE + EQUIPMENT_IN_SPACE）
- 首页统计支持窗类设备分组
- 后端保证设备列表数据齐全（equipType + runStatus + relObjIds），前端自行计算状态

**Non-Goals:**
- 不处理运行日志扩展
- 不新增 VentilationSpaceStatusEnum 枚举值
- 不改动前端状态展示逻辑
- 不改动 CacheObjectExtensionFieldConfig（窗类无扩展字段需求）
- 不改动 RwdObjectBusinessFeildDTO（窗类自动通过 enableVentilationFunType）

## Decisions

### D1: filtrationId() 保留方法，返回空集

**决策**: `RunningMonitorVentilationStrategy.filtrationId()` 方法签名保留，方法体改为 `return Collections.emptySet()`，原逻辑注释保留。

**理由**: 兼容未来可能恢复剔除规则的场景，不破坏 `getDtpRwdEquipmentDTOS()` 的调用链。调用点的 `removeIf(filtrationIds::contains)` 在空集下天然变为空操作。

**替代方案**: 直接删除方法 → 改动范围更大，且未来恢复需重新编写。

### D2: 状态计算由前端处理

**决策**: `VentilationSpaceStatusEnum` 不新增枚举值。后端 `spaceStatus()` 保持现有 5 状态计算逻辑（对穹顶仅窗场景存在 fall-through）。前端从设备列表的 `equipType` + `runStatus` + `relObjIds` 自行聚合送风/排风/自然通风状态。

**理由**: PRD 定义的"自然通风状态"是空间卡片级别的独立状态维度，与现有"空间通风状态"（SEND_AND_REMOVE/SENDING/REMOVING）正交。后端强行合并会导致语义混乱。

**已知问题**: 穹顶空间仅关联窗类设备且至少一个 runStatus=运行时，`spaceStatus()` 会 fall-through 不设值。前端需兜底此场景。

### D3: 窗类仅在 DOME_VENTILATION 类型走 EQUIPMENT_IN_SPACE

**决策**: `listSpaces()` 方法仅在 `DOME_VENTILATION` 分支新增窗所在空间查询。其他三种二级类型（LOGISTICS/PARKING/TOILET）不受影响。

**理由**: PRD 明确窗类"通过 MechInArch/Eq2Sp 所在空间关系关联 SFT-01.05.00 挑空及其下级类型的穹顶"，窗只进入穹顶空间。

### D4: cache_object_rel 存两种关系类型

**决策**: 写入侧 `relationsByType` 新增 `EQUIPMENT_IN_SPACE` 关系。读取侧发起两次 `listReverse` 查询（不同 graphCode/relCode），结果合并到同一个 `mapRelObjIdAndData`。

**理由**: `CacheObjectRelQueryDaoParam` 只支持单 graphCode/relCode 查询，不支持 OR 条件。两次查询的开销可忽略（均为内存级 DAO 操作）。

**替代方案**: 扩展 DAO 支持多条件 → 改动公共基础设施，范围过大。

### D5: VentilationEquipStatisticsEnum 新增两个窗类分组

**决策**: 新增 `CFCSWN_NATURAL("CFCSWN_NATURAL", "CFCSWN", ...)` 和 `CFCSEW_NATURAL("CFCSEW_NATURAL", "CFCSEW", ...)` 两个枚举值。

**理由**: `queryEquipGroupId()` 按 classCode 分组到 `VentilationEquipStatisticsEnum`，用于首页卡片和 IoT 统计接口。窗类无 funcType 区分，各独立分组。PRD 5.2.2 明确要求设备统计顺序包含普通窗和排烟窗。

### D6: VentilationEquipTypeEnum 新增 NATURAL_VENTILATION

**决策**: 新增 `NATURAL_VENTILATION("NATURAL_VENTILATION", "自然通风")` 枚举值。

**理由**: `equipType()` 方法需要为 CFCSWN/CFCSEW 映射到统一的设备类型，前端据此分类展示。

### D7: 窗类设备查询使用 listEquipmentInBySpaceIds

**决策**: 窗类设备通过 `dtpRwdClientService.listEquipmentInBySpaceIds(spaceIds, param)` 查询，该方法内部使用 `EQUIPMENT_IN_SPACE` 关系（MechInArch/Eq2Sp）。

**理由**: 与现有 `listEquipmentServiceBySpaceIds`（MechForArch/Eq2Sp）平行，API 已存在且被其他模块（安防、照明）使用。

## Risks / Trade-offs

**[Risk] fall-through 场景** → 穹顶仅窗空间在 `spaceStatus()` 中 spaceStatus 为 null。前端需兜底，若老前端未处理则展示空白。
  - **缓解**: 确认老前端对 null spaceStatus 的容错性；新前端按 PRD 自行计算。

**[Risk] 缓存重建依赖** → 变更发布后需触发一次 VENTILATION 缓存重建，否则新缓存中不含窗类设备和 EQUIPMENT_IN_SPACE 关系。
  - **缓解**: `CacheTemplateRebuildService` 定时重建机制会自动生效；也可发布后手动触发。

**[Risk] IoT 统计 API 兼容性** → `VentilationIotServiceCloud` 调用 `watchFanStatistics` 接口时传入窗类设备 ID，该接口是否支持窗类设备的统计。
  - **缓解**: 确认 DTP 侧 watchFanStatistics 对未知设备类的容错性（应返回空统计）。

**[Trade-off] 两次 listReverse 查询** → 读取路径多一次 DAO 查询，增加微量延迟。
  - **接受**: 均为内存/Redis 级操作，延迟 < 1ms，可忽略。
