# 【PRD-290902】移除部分末端管理的设备类

## Goal

重新定义末端和通风管理的设备范围，将三类设备从末端管理范围中移除，归到通风模块进行管理。

## Requirements

### R1: 通风风机(ACVTSF)挑空设备移除
- 通风风机(ACVTSF)服务的物业分区为空间功能类型 SFT-01.05.00"挑空"的设备，从末端管理范围中移除
- 影响：末端策略中的"③ 动态归入"逻辑（ACVTSF serving SFT-01.05.00 挑空 spaces）需要移除

### R2: 排风兼机械排烟风机(FFSCAF)挑空设备移除
- 排风兼机械排烟风机(FFSCAF)服务的物业分区为空间功能类型 SFT-01.05.00"挑空"的设备，从末端管理范围中移除
- 影响：末端策略中的 queryFFSCAFBySpace 逻辑（FFSCAF serving 挑空）需要移除

### R3: 排烟窗(CFCSEW)完全移除
- 排烟窗(CFCSEW)彻底从末端管理范围中移除
- 影响：从 ALL_EQUIPMENT_CLASS_CODES、LocalCacheConstant 等配置中移除

### R4: 响应范围限制
- 配置表和运行日志相关不在本次任务范围内
- 仅处理后端业务逻辑和前端展示

### R5: 前端同步移除
- 前端 ACOperationControl 模块中移除对应设备类的排序映射和图标配置
- 前端仓库：D:\WebstormProjects\meos-control-web

## Acceptance Criteria

- [ ] 后端 RunningMonitorAirTerminalStrategy 不再返回服务挑空空间的 ACVTSF 和 FFSCAF
- [ ] 后端 RunningMonitorAirTerminalStrategy 不再返回 CFCSEW 设备
- [ ] 后端 AirTerminalService.ALL_EQUIPMENT_CLASS_CODES 不再包含 CFCSEW
- [ ] 后端 LocalCacheConstant.AIR_TERMINAL_OBJECT_CLASS 不再包含 CFCSEW
- [ ] 前端 ACOperationControl 设备统计排序不再包含 ACVTSF、FFSCAF、CFCSEW
- [ ] 前端 ACOperationControl 设备卡片图标不再包含 ACVTSF、FFSCAF、CFCSEW
- [ ] 通风模块功能不受影响（ACVTSF、FFSCAF 仍由通风管理）

## Notes

- ACVTSF、FFSCAF 只是从末端管理移除，仍由通风模块管理
- CFCSEW 是彻底从末端管理移除（原不由通风管理）
- 只改动后端业务逻辑和前端展示层，不涉及配置表和日志