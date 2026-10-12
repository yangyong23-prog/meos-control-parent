# 【PRD-290902】移除部分末端管理的设备类 - 设计文档

## 1. 概述

### 1.1 范围
- **后端仓库**: `d:\IdeaProjects\meos-control-parent`
- **排除范围**: 配置表变更、运行日志相关不受影响

### 1.2 设备类处理方式
| 设备类 | 处理方式 | 说明 |
|--------|---------|------|
| ACVTSF (通风风机) | 条件移除 | 仅移除服务挑空(SFT-01.05.00)空间的设备，其他仍然保留 |
| FFSCAF (排风兼机械排烟风机) | 条件移除 | 仅移除服务挑空(SFT-01.05.00)空间的设备，其他仍然保留 |
| CFCSEW (排烟窗) | 完全移除 | 从末端所有配置中彻底移除 |

## 2. 后端设计

### 2.1 数据流现状

#### 末端设备查询链路（RunningMonitorAirTerminalStrategy）
```
queryAllEquipments()
├── queryBasicEquipments()        ← ALL_EQUIPMENT_CLASS_CODES - {ACVTSF,FFSCAF,ACVVEJ,ACVVER,ACVVEA}
├── queryFFSCAFBySpace()          ← FFSCAF serving 挑空 spaces  ← 需移除
├── queryACVTSFThreeWay()          ← ACVTSF 3路并集
│   ├── ① funcType=8 接力风机       ← 保留
│   ├── ② funcType=10 排风机       ← 保留
│   └── ③ 动态归入(挑空)           ← 移除
├── queryACVVEJByACZone()         ← 保留
├── queryACVVERThreeWay()         ← 保留
└── queryACVVEAЕRecursive()       ← 保留
```

### 2.2 改动点

#### P0-1: `RunningMonitorAirTerminalStrategy` - 移除 queryFFSCAFBySpace
- 文件: `common/.../structure/common/strategy/impl/RunningMonitorAirTerminalStrategy.java`
- 操作: 删除 `queryAllEquipments()` 中对 `queryFFSCAFBySpace()` 的调用（lines 644-654）
- 效果: FFSCAF 不再因服务挑空空间而被纳入末端管理

#### P0-2: `RunningMonitorAirTerminalStrategy` - 移除 ACVTSF 动态归入
- 文件: `common/.../structure/common/strategy/impl/RunningMonitorAirTerminalStrategy.java`
- 操作: 删除 `queryACVTSFThreeWay()` 中的 ③ 动态归入部分（lines 867-883）
- 效果: ACVTSF 不再因服务挑空空间而被纳入末端管理

#### P0-3: `AirTerminalService` - 移除 CFCSEW
- 文件: `common/.../service/airterminal/AirTerminalService.java`
- 操作: 从 `ALL_EQUIPMENT_CLASS_CODES` 列表中移除 `DtpRwdClassCodeEnum.CFCSEW`
- 效果: 末端全量设备类清单不再包含排烟窗

#### P0-4: `LocalCacheConstant` - 移除 CFCSEW
- 文件: `common/.../constant/LocalCacheConstant.java`
- 操作: 从 `AIR_TERMINAL_OBJECT_CLASS` 集合中移除 `"CFCSEW"`
- 效果: 对象所属模块判断不再将 CFCSEW 归为末端

### 2.3 影响分析

| 消费者 | 影响 | 处理方式 |
|--------|------|---------|
| TerminalStatisticsServiceImpl | 间接通过 `listEquipments()` 查询 | 自动跟随策略变化，无需额外修改 |
| AirTerminalHomePageCardDataHandler | 同上 | 自动跟随 |
| AirStatisticsServiceAbstract | 同上 | 自动跟随 |
| AirTerminalServiceImpl (设备统计/空间查询) | 同上 | 自动跟随 |
| MdQueryObjStrategy (运行日志) | CFCSEW 在 classList 中 | 配置表+日志相关，用户指定不处理 |
| RunAnalysisServiceImpl.TERMINAL_CLASS_CODES | 仅含 ACATAH/ACATFU/ACATFC | 无影响 |

## 3. 边界条件

### 3.1 向后兼容
- 缓存重建：CacheRwdAirTerminalStrategy 依赖 `listFullSpaceEquipmentForCacheBuild`，该接口最终经过 `RunningMonitorAirTerminalStrategy` 的 `listEquipments()` → 策略变化后缓存重建会自动刷新
- 无需数据迁移

### 3.2 注意事项
- `queryBasicEquipments()` 中的 `specialEquipmentClassCodes` 仍保留 ACVTSF/FFSCAF（它们有各自的专用查询逻辑，移除的只是挑空部分）
- `queryACVTSFThreeWay` 中 ① 接力风机和 ② 排风机逻辑保持不变，不受影响
- FFSCAF 的非挑空设备不受影响（原本就不在末端管理范围中）
- ACVTSF 和 FFSCAF 仍保留在 `ALL_EQUIPMENT_CLASS_CODES` 中，仅在策略层面过滤挑空设备