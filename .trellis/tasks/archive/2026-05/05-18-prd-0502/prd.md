# 空调末端设备与空间管理范围适配 PRD-0502

## Goal

根据 PRD-0502 需求，扩展末端系统的设备管理范围（19种设备类），重点新增 4 种设备类（ACAHG、ACVVER、ACVVEA、ACATHT）和修改 ACVTSF 查询逻辑，使设备实例范围查询符合产品要求。

## What I already know

- 现有代码管理 14 种设备类，PRD 要求 19 种，缺少 4 种：ACAHG、ACVVER、ACVVEA、ACATHT
- `DtpRwdClassCodeEnum` 中不存在这 4 个枚举值
- `ALL_EQUIPMENT_CLASS_CODES` 中不含这 4 种
- `DtpRwdRelationTypeEnum` 中缺少 ACAirNetwork/* 和 VentNetwork/Discharge 关系类型
- ACVTSF 现有查询逻辑：FreshAirNetwork 关联 ACATAH + SFT-01.05.00 服务空间，不符合 PRD 要求的 3 路并集
- 空间管理范围已正确实现，无需修改
- `CommonConst.AIR_TERMINAL_CLASSCODES` = {"ACATAH", "ACATFU", "ACATFC"}，可能需加入 ACAHG/ACATHT
- ACAHG（空调机组控制组）类似于已有的 ACCPC/ACOTC 等控制组设备
- ACVVER（电动调节风阀）需要 3 路图关系并集查询
- ACVVEA（电动通断风阀）需要递归图关系查询（VentNetwork/Discharge 双向）
- ACATHT（风管式再热装置）为基本查询设备

## Assumptions (temporary)

- ~~图关系引擎（RWD graph 接口）已支持 ACAirNetwork、VentNetwork 等图类型~~ ✅ 已确认：物理世界已定义，仅代码未接入
- 递归查询的终止条件为"找不到更多设备"
- ACVVEA 的递归深度上限为 10 层，超过时记录警告日志并终止
- ACAHG 的查询逻辑与基本设备一致（设备服务空间 + 监控过滤）

## Open Questions

1. ~~ACAirNetwork/VentNetwork 图关系在 RWD 接口中是否已可用？~~ ✅ 已确认可用，物理世界已定义
2. ~~ACVVER 查询路径②中"独立空调机组"的判断（不属于任何空调机组控制组），需通过 EQUIP_IN_GROUP 反向查询确认，现有策略类是否支持此类反向排除？~~ ✅ 采用差集方式：全量 ACATAH ID 减去 EQUIP_IN_GROUP(from=ACAHG, to=ACATAH) 结果
3. ~~CommonConst.AIR_TERMINAL_CLASSCODES 是否需要加入 ACAHG/ACATHT？该常量的消费场景是什么？~~ ✅ 所有新增设备类均属空调末端，全部加入 AIR_TERMINAL_CLASSCODES（ACAHG/ACATHT/ACVVER/ACVVEA），VENTILATION_CLASSCODES 不变
4. ~~ACVVEA 递归查询性能问题：是否需要限制递归深度或设置超时？~~ ✅ 设递归深度上限 10 层，超过时记录警告日志并终止

## Requirements (evolving)

### P0：核心设备范围变更

1. 新增 `DtpRwdClassCodeEnum` 枚举：ACAHG、ACVVER、ACVVEA、ACATHT
2. 加入 `ALL_EQUIPMENT_CLASS_CODES` 列表
3. 新增 `DtpRwdRelationTypeEnum`：ACAirNetwork/Supply、ACAirNetwork/Return、ACAirNetwork/ReandEx、VentNetwork/Discharge
4. 实现 ACVVER 图关系查询（3 路并集）
5. 实现 ACVVEA 递归图关系查询（VentNetwork/Discharge 双向）
6. 修改 ACVTSF 查询逻辑（3 路并集 + funcType 过滤 + ACAirNetwork 支持）

### P1：基本设备接入

7. 新增 ACAHG 基本查询（设备服务空间 + 监控过滤）
8. 新增 ACATHT 基本查询（设备服务空间 + 监控过滤）

### P2：关联配置更新

9. 确认并更新 `CommonConst.AIR_TERMINAL_CLASSCODES`
10. 确认并更新 `CommonConst.VENTILATION_CLASSCODES`（如需）

## Acceptance Criteria (evolving)

- [ ] 4 种新设备类枚举已添加到 `DtpRwdClassCodeEnum`
- [ ] `ALL_EQUIPMENT_CLASS_CODES` 包含 18 种设备类（含新增 4 种）
- [ ] ACAirNetwork 和 VentNetwork 关系类型枚举已添加
- [ ] ACVVER 查询能正确返回 3 路并集结果
- [ ] ACVVEA 查询能递归遍历 VentNetwork/Discharge 双向关系
- [ ] ACVTSF 查询逻辑符合 PRD 3 路并集要求
- [ ] ACAHG 和 ACATHT 可通过基本查询正确获取
- [ ] 编译通过，无 PMD/Checkstyle 违规

## Definition of Done

- 代码编译通过
- PMD/Checkstyle 无新增违规
- 相关枚举和常量同步更新
- 图关系查询逻辑有日志记录
- 无需新增数据库迁移（本任务仅涉及代码逻辑）

## Out of Scope

- 配置表更新（设备详情、空间详情、hover、列表等配置）— 另行处理
- 运行日志接入 — 另行处理
- 半剖图升级 — 另行处理
- 压差标准适配 — 另行处理
- 前端 UI 变更 — 另行处理

## Decision (ADR-lite)

**Context**: 需要新增 4 种设备类查询和修改 ACVTSF 查询逻辑，现有 `getDtpRwdEquipmentDTOS()` 方法已较长且逻辑内联
**Decision**: 采用方式 B（策略类模块化重构），将每种特殊设备查询逻辑抽取为独立 private 方法，主方法仅做编排
**Consequences**: 代码可读性和可维护性提升；改动范围比增量扩展稍大，但不改变整体架构；每个方法可单独调试

### 关键文件

- `DtpRwdClassCodeEnum.java` — 新增枚举
- `DtpRwdRelationTypeEnum.java` — 新增关系类型
- `RunningMonitorAirTerminalStrategy.java` — 核心查询逻辑
- `AirTerminalService.java` — `ALL_EQUIPMENT_CLASS_CODES` 常量
- `CommonConst.java` — `AIR_TERMINAL_CLASSCODES` / `VENTILATION_CLASSCODES`

### 现有查询模式

当前 `getDtpRwdEquipmentDTOS()` 分 4 组并行查询：
1. 基本设备（排除 ACVTSF/FFSCAF/ACVVEJ）
2. 挑空空间关联的 ACVTSF/FFSCAF
3. ACZone 关联的 ACVVEJ
4. FreshAirNetwork 关联 ACATAH 的 ACVTSF

### PRD 要求的查询模式

1. 基本设备（排除 ACVTSF/FFSCAF/ACVVEJ/ACVVER/ACVVEA）+ ACAHG/ACATHT
2. 挑空空间关联的 FFSCAF（独立方法 queryFFSCAFBySpace）
3. ACZone 关联的 ACVVEJ（独立方法 queryACVVEJByACZone）
4. ACVTSF 3 路并集（独立方法 queryACVTSFThreeWay）：
   - ① 接力风机：ACAirNetwork/Supply(from=ACATAH, to=ACVTSF) + FreshAirNetwork/Fresh(from=ACATFU, to=ACVTSF)，funcType=8
   - ② 排风机：funcType=10 + EQUIPMENT_SERVICE_SPACE
   - ③ 动态归入：不满足①且 SFT-01.05.00 + EQUIPMENT_SERVICE_SPACE
5. ACVVER 3 路并集（独立方法 queryACVVERThreeWay）：
   - ① 控制组关联：ACAirNetwork/Supply(from=ACAHG, to=ACVVER) + ACAirNetwork/Return(from=ACVVER, to=ACAHG) + ACAirNetwork/ReandEx(from=ACVVER, to=ACAHG)
   - ② 独立空调机组关联：差集ACATAH + ACAirNetwork/Supply(from=独立ACATAH, to=ACVVER) + ACAirNetwork/Return(from=ACVVER, to=独立ACATAH)
   - ③ 新风机组关联：FreshAirNetwork/Fresh(from=ACATFU, to=ACVVER)
6. ACVVEA 递归查询（独立方法 queryACVVEARecursive）：
   - Step1: 查排风机 ACVTSF(funcType=10) + EQUIPMENT_SERVICE_SPACE
   - Step2: VentNetwork/Discharge 双向查询关联阀门
   - Step3: 递归 Step2 直到无新设备

### i18n key

新增枚举值需在 `messages_en.properties` / `messages_zh.properties` 中添加对应的 i18n key
