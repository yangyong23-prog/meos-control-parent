# 安防静态对象与关系缓存接入

## 目标

为安防运行监测建立可复用的项目级设备、空间和设备-空间关系缓存，并将基础静态查询切换到缓存，减少首屏期间重复访问 RWD 和全量过滤。

## 范围

- 设备缓存 Key：`SECURITY_LIST_EQUIPMENT`，覆盖门禁、摄像头、入侵报警和其他共 12 类安防设备。
- 空间缓存 Key：`SECURITY_LIST_SPACE`，保存空间本地名称及建筑、楼层关联字段。
- `cache_object_rel` 关系：`SPIACU` 服务空间使用 `service_space`；门禁和有布撤防能力摄像头所在空间使用 `in_space`。
- 设备字段保留前端当前设备接口消费的 `bimId`、`bimLocation`，与普通设备字段在同一响应中返回；`outline` 仅保留在空间模型视图和空间响应中，不新增模型图专用 HTTP 接口，也不要求前端二次合并。
- 对象、Key 关联、关系和就绪状态的全量初始化、增量刷新、删除清理与 READY/未 READY 读取路由。
- 切换 `/security/function`、`/security/listDeploymentSpace`、`/security/listEquipment` 的静态数据读取，并为 dashboard/listAccessSpace 提供静态候选数据。

## 约束

- 所有写入和读取必须带项目范围；关系方向和去重遵循统一缓存对象模型。
- 动态 IoT 值、孪生规则、异常事件不得写入静态对象缓存。
- READY 时不再查询同一批 RWD 静态对象、建筑楼层或设备-空间关系；未 READY 时整条逻辑走原 RWD 路径，不能混用不完整缓存。
- 数据库、转换或反序列化异常不能伪装成缓存未命中。

## 验收标准

- [ ] AC1：初始化后设备、空间、扩展静态字段和两类关系按项目准确落库，重复关系去重，删除或关系迁移不残留旧记录。
- [ ] AC2：完整写入成功后才标记 SECURITY READY；任一步失败不会把不完整范围暴露给读取方。
- [ ] AC3：READY 路径的 `function`、`listDeploymentSpace`、`listEquipment` 结果与原路径一致，且通过调用计数证明不访问同批 RWD 静态查询。
- [ ] AC4：未 READY、缓存空结果、部分关系缺失和基础设施异常分别按约定处理；明确异常不会静默返回空列表。
- [ ] AC5：设备接口一次返回普通字段及 `bimId`、`bimLocation`，撤布防空间继续返回空间 `outline`；前端现有 BIM 过滤结果与改造前一致。
- [ ] AC6：单元/集成测试覆盖项目隔离、关系方向、批量查询、空集合、刷新失败和回退路径；受影响模块编译通过。

## 非目标

- 不接入 `ARMING`/`PASSING` 需求配置结果计算。
- 不优化 IoT 信息点、规则匹配或设备状态统计聚合。
- 不改变对外接口路径和前端现有模型图消费方式。

## 依赖

- 父任务：`08-12-security-monitoring-performance`。
- 统一缓存对象、关系、模型视图和 READY 基础设施已可用。
- 子任务 `security-dashboard-demand`、`security-dynamic-statistics` 依赖本子任务提供的缓存范围和读取契约。
