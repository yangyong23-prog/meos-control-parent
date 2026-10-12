# 模型视图读取切换

## Goal

仅切换 `querySpace`、`listSpaceOutline` 等模型图消费者到模型视图缓存，并确保查询不依赖对象表。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01 至 16 已通过，直接依赖子任务 02、04、08、15。

## Requirements

- 继承父任务统一异常边界：业务校验和范围冲突使用带中英文资源的 `BusinessException`；JSON 转换统一使用 `ObjectMapperUtils` fail-safe，Converter 不自行捕获 Jackson 异常；读取方对 `null` 整批降级，写入方在删除前拒绝 `null`，Mapper 和事务异常不得吞掉。
- 在模型图读取入口复用子任务 15 的集中路由策略，不在消费者中散落支持类型或 READY 判断。
- 选中新模型后，按项目、对象 ID、建筑或楼层等既有条件批量查询 `cache_object_model_view`，不得 JOIN `cache_object` 或读取 `object_data`。
- 公共模型视图属性只从本表独立列重建，再合并 `view_data`；扩展 JSON 不得覆盖公共列。
- `view_data` 的字段名、字段类型、空值和轮廓等大字段应与旧链路返回保持业务一致。
- 模型视图公共属性从本表独立列恢复；`view_data` 的全部 JSON 字段只由独立
  classCode Map 决定，不按 objType 注入固定基础字段。`bimId`、`bimLocation`、
  `outline` 等字段必须在对应 classCode 中明确配置。
- 保持 `querySpace`、`listSpaceOutline` 及已盘点模型图消费者的过滤、顺序、去重和空结果契约。
- 缺少所需视图时按对应消费者的空结果契约返回，不混入旧对象、旧模型或 RWD 结果。
- 末端、照明未 READY 时按对应接口实时查询 RWD；其他运行监测系统继续使用原读取链路。

## Out Of Scope

- 不修改对象、模板对象明细或 IOT 规则读取。
- 不修改模型视图写入、字段 Map、初始化、核验或集中路由规则。
- 不通过 JOIN 或额外对象查询补齐模型视图字段。

## Acceptance Criteria

- [x] `querySpace`、`listSpaceOutline` 和已盘点消费者的新旧结果对账通过，覆盖建筑、楼层、对象类型和空结果。
- [x] 公共列与 `view_data` 可正确重建结果，覆盖未配置 Map、映射外字段、显式空值和轮廓大字段。
- [x] 查询 SQL 只访问 `cache_object_model_view`，不 JOIN `cache_object`、不读取 `object_data`，且使用批量和按需投影。
- [x] READY 后缺失视图时不混读旧表或 RWD；未 READY 实时查询，其他系统行为不变。
- [x] 本任务 diff 只包含模型视图消费者接入、必要查询调整和聚焦测试。

## Review Gate

完成实现和质量检查后暂停，等待用户审核模型图结果与无 JOIN 查询；审核通过前不得开始子任务 18。
