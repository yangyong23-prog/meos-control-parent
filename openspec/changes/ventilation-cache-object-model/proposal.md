## Why

VENTILATION（通风）的设备缓存和空间缓存仍使用旧表 `cache_rwd_object`，写入时需 12 次 Feign 调用（空间查询+设备查询+需求配置+空调季+重复查询），构建耗时长且存在重复调用。末端（AIR_TERMINAL）和照明（LIGHTING）已完成新缓存对象模型切换，VENTILATION 是最后一个需要迁移的一级类型。

## What Changes

- VENTILATION 加入新缓存对象模型支持范围（`CacheObjectModelStatusServiceImpl.SUPPORTED_RUNNING_MONITOR_TYPES`）
- 设备缓存写入从旧表 `cache_rwd_object` 切换到新表 `cache_object` + `cache_object_model_view` + `cache_object_key_rel`，同时写入设备→空间关系到 `cache_object_rel`
- 空间缓存写入从旧表切换到新表，存储原始 `DtpRwdSpaceDTO`（不存复合 `VentilationDtpRwdSpaceVO`），绕过 SPACE classCode 白名单字段投影限制
- 空间读取路径重构：READY 后从新缓存读取原始空间+设备 DTO + 关系，需求配置和空调季在读取时实时查询组装，由 Redis 短缓存兜底
- 设备读取路径（`duty/homePage`）增加 READY 判断，READY 后从新缓存读取 `DtpRwdEquipmentDTO`
- 新增 2 个缓存键常量：`VENTILATION_LIST_SPACE` 和 `VENTILATION_LIST_EQUIPMENT`
- 信息点配置查询（`InfoCodeConfigServiceImpl`）、信息点网关（`InfoCodeGatewayImpl`）、模板重建（`CacheTemplateRebuildService`）自动走新缓存路径，无需改动

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `cached`: VENTILATION 缓存从旧表切换到新缓存对象模型，涉及缓存键配置、写入策略、读取路径和 READY 状态启用范围

## Impact

- **`CacheObjectModelKeyConfig`**: 新增 2 个缓存键常量
- **`CacheObjectModelStatusServiceImpl`**: SUPPORTED_RUNNING_MONITOR_TYPES 加入 VENTILATION
- **`CacheRwdVentilationStrategy`**: 重写 `buildCacheData()`，空间和设备分别写入新表
- **`RunningMonitorVentilationStrategy`**: 重写 `listDashboardSpaces()`，READY 后走新缓存读取+内存组装
- **`AbstractVentilationIotService`**: 重写 `listEquipments(boolean)`，READY 后走新缓存读取
- **`InfoCodeConfigServiceImpl`**: 自动生效（`shouldUseNewModel` 返回 true）
- **`InfoCodeGatewayImpl`**: 自动生效（`mapTemplateTypeAndObjects` 走新缓存）
- **`CacheTemplateRebuildService`**: 自动生效（`prepareTemplate` 走新路径）
- 写入 Feign 调用从 12 次降至 4 次（-67%）
- 读取缓存命中时（99%）0 Feign，与旧方案持平
- 读取缓存未命中时（1%）从 10 Feign 降至 5 Feign（-50%）
