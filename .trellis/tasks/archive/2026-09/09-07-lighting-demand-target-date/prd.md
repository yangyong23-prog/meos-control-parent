# 照明getDemandTarget接口增加日期入参并标记外部服务调用

## Goal

感知控制服务（dtp-meos-server-edge）需要按任意日期查询照明需求目标。为 `/lighting/getDemandTarget` 增加日期入参，使模板、场景、临时控制任务的"当天"计算全部支持按入参日期回查；同时在接口文档上标记外部调用方。

## Requirements

- `/lighting/getDemandTarget` 请求参数 `LightingDemandTargetQueryParam` 新增日期字段：
  - 类型 `LocalDate`，字段语义与 `BaseDemandTemplateParam.date` 一致，命名 `date`
  - **选填**：不传时保持现状，缺省为系统当天（`DateUtils.now().toLocalDate()`），向后兼容现有前端调用
- 服务实现 `AbstractLightingService.getDemandTarget` 中，内部日期统一取自入参（缺省当天），替换以下四处对"系统当天"的使用：
  - 需求模板查询（`templateParam.setDate(today)` → `queryAndGetTemplate`）
  - 场景查询（`queryScenes(template, today, ...)` → `listValidScenesByDate`）
  - 临时控制任务过滤（`queryInterventionTasks` 内部 `[00:00:00, 23:59:59]` 当日窗口 `beginTime` / `todayRange`）
  - `funcTypeConfigs` 汇总窗口（`fillFuncType2CircuitSetting` 内部 `DateUtils.now()` 计算的 0 点至次日 0 点窗口，任务时间段裁剪与输出分段均依赖它，需改为目标日期窗口；场景时间段由 `fillSceneRealTime(date)` 生成、已落在目标日期，无需处理）
- 接口 `@Operation` tags 标记外部调用：保留 `V7_260101_0`，新增 `ExternalServerConstant.DTP_MEOS_SERVER_EDGE`（`ExternalServerConstant.java:50`，感知控制服务 dtp-meos-server-edge）

## Acceptance Criteria

- [ ] `LightingDemandTargetQueryParam` 新增选填 `LocalDate date` 字段，带 `@Schema` 描述（参照 `ValueSelectQueryParam.date` 写法）
- [ ] 不传 `date` 时，`getDemandTarget` 行为与现状完全一致（模板/场景/临时任务/`funcTypeConfigs` 均按系统当天）
- [ ] 传入 `date` 时，模板查询、场景查询、临时任务当日窗口、`funcTypeConfigs` 汇总窗口四处均按该日期计算，输出时间段落在目标日期当天
- [ ] `LightingController.getDemandTarget` 的 `@Operation` tags 为 `{V7_260101_0, DTP_MEOS_SERVER_EDGE}`，import 按项目惯例添加
- [ ] 相关模块编译通过
