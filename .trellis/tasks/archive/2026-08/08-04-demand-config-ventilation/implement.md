# 实现计划

## 改动清单

### 1. 支持范围扩展
- 文件：`DemandConfigResultRefreshRepositoryImpl.java` (L85-86)
- 改动：`SUPPORTED_DEMAND_TYPES` 添加 `DemandTypeEnum.VENTILATION`
- 自动派生 `SUPPORTED_TEMPLATE_TYPES` 覆盖通风 4 个模板类型

### 2. 定时任务参数扩展
- 文件：`DemandConfigResultInitTaskParam.java`
- 改动：新增 `demandTypes` 字段（`LinkedHashSet<DemandTypeEnum>`），为空时初始化所有已支持类型

### 3. 定时任务过滤逻辑
- 文件：`DemandConfigResultInitTaskHandler.java` (L116-119)
- 改动：遍历 `listSupportedTemplateTypes()` 时过滤 `demandTypes` 参数指定的类型
- 新增 `filterTemplateTypes()` 方法：空 demandTypes 返回全部，非空取交集

## 不需要改动

- 事件监听器（DemandConfigResultRefreshEventListener）：已通过 `supportsTemplateType()` 自动覆盖
- `removeAirconditioningQueryFields()`：已有 `templateType != AIRCONDITIONING` 判断，通风直接跳过
- 通风策略类：FIELD 继承、默认值逻辑均兼容预计算
