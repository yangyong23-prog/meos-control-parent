# 通风需求接入需求配置结果预计算

## 背景

需求配置结果预计算（demand_config_result）当前仅支持空调（AIRCONDITIONING）。
通风需求（VENTILATION）尚未接入，配置结果仍走实时继承链计算。

空调已在上个版本完成初始化和部署，本次升级只需初始化通风。
当前定时任务空参数表示全量初始化所有已支持类型，需要新增 demandTypes 参数以支持只执行指定类型。

## 需求

1. 将 `DemandTypeEnum.VENTILATION` 加入预计算支持范围
2. 定时任务参数新增 `demandTypes` 字段，支持只初始化指定需求类型
3. 空参数时初始化所有已支持类型（保持向后兼容）

## 验收标准

1. `SUPPORTED_DEMAND_TYPES` 包含 `VENTILATION`，通风的 4 个模板类型全部自动纳入
2. 定时任务传 `{"demandTypes": ["VENTILATION"]}` 时只执行通风初始化
3. 定时任务空参数时执行所有类型（空调 + 通风）初始化
4. 配置变更、模板变更、DTX 发布等事件驱动刷新自动覆盖通风
5. `removeAirconditioningQueryFields` 不影响通风（已有类型判断保护）
