# 安防运行监测性能优化

## 目标

降低安防运行监测页面首屏与 SSE 设备统计链路的响应时间。稳定的设备、空间、设备-空间关系和需求配置改为读取项目已有的缓存对象模型与预计算结果；IoT 信息点、规则和异常事件继续保持实时语义，并通过缩小查询范围和优化聚合算法降低耗时。

改造后必须保持接口请求、响应字段、空间判定、设备分组、需求继承和状态统计结果兼容，并支持缓存尚未就绪时的完整降级。

## 子任务地图

本父任务负责统一需求、依赖顺序和跨子任务验收，不直接承载全部实现：

| 子任务 | 责任范围 | 依赖 |
|---|---|---|
| `08-12-security-static-cache` | 安防设备、空间、设备-空间关系缓存；基础静态接口读取 | 无 |
| `08-12-security-dashboard-demand` | 仪表板静态关联和 `ARMING`/`PASSING` 需求配置结果接入 | 静态缓存子任务 |
| `08-12-security-dynamic-statistics` | 通行空间规则聚合、设备统计、IoT/SSE 动态聚合 | 静态缓存子任务 |

真实 HTTP、数据库、RWD、IoT/SSE 和性能复测依赖可访问的部署环境，不再建立代码开发子任务；相关验证作为发布前环境检查项，由具备固定项目快照和缓存状态控制能力的环境执行。

模型图字段不作为单独子任务或独立接口改造项。本期沿用前端当前调用的设备接口，在同一响应中返回设备普通字段和 `bimId`、`bimLocation`、`outline` 等模型图字段；不要求前端新增请求、二次合并或改变现有消费方式。底层若已有模型视图存储，可在服务端内部批量读取后一次组装，不改变对外契约。

## 背景与性能基线

同一项目样本的现有观测如下。实施阶段需在相同数据集、部署规格和采样方法下重新采集优化前后数据。

| 优先级 | 接口或事件 | 总耗时 | 主要瓶颈 |
|---|---|---:|---|
| 最高 | `POST /security/equipment/statistics`、`SECURITY_EQUIPMENT_STATISTICS` | 23.443s | 查询 4 个 IoT 信息点耗时 21.285s，占 90.8% |
| 高 | `POST /security/dashboard`、`SECURITY_DASH_BOARD` | 18.807s | 需求配置 9.964s；关联安防设备 5.875s |
| 高 | `POST /security/listAccessSpace` | 14.241s | 规则与空间过滤 9.849s，占 70.7% |
| 中 | `POST /security/listEquipment` | 3.980s | 设备筛选 1.918s |
| 中 | `POST /security/listDeploymentSpace` | 2.997s | 摄像头所在空间查询 1.090s |

页面加载还会使用 `/security/function`、`/running-monitor/batchQueryInfoCode`、`/abnormalEvent/listAbnormalEvent` 和需求配置查询；这些链路的静态依赖应复用同一份缓存数据，动态结果不得固化为静态对象缓存。

## 业务口径

### 安防设备范围

全量安防设备包含以下 12 个 `classCode`：

- 门禁：`CFCSDR`、`CFCSFR`、`SPEGFG`。
- 摄像头：`SPVSCM`、`WEGMCM`。
- 入侵报警：`SPIACU`、`SPIAGD`、`SPIAID`、`SPIAMA`。
- 其他：`SPEGCU`、`SPVSVE`、`SPVSDR`。

设备统计只覆盖门禁 3 类、`SPIACU`、`SPVSCM`、`WEGMCM` 共 6 类。现有 `monitoringSurveillanceStatus`、实时流协议、摄像头类型和布撤防能力等过滤口径必须保持不变。

### 空间判定

- 撤布防空间：存在 `SPIACU` 服务关系，或存在 `hasArmingFunc = "1"` 的 `SPVSCM` 所在关系。
- 通行空间：空间下存在门禁设备，且至少一个关联门禁具有 `accessDemand` 规则、至少一个关联门禁具有 `equipOnSet` 规则；两个规则可来自不同门禁。
- `ARMING` 需求配置保留布防、撤防时间区间语义；`PASSING` 保留全通行、全禁行和按权限通行时间区间语义。
- 空间需求配置继续支持 `PROJECT -> BUILDING -> FLOOR -> SPACE` 四级继承，空间自身配置优先。

## 需求

### R1. 建立安防静态数据缓存范围

- 使用 `SECURITY_LIST_EQUIPMENT` 维护项目级安防设备范围，至少持久化 `id`、`local_name`、`class_code`、`monitoring_surveillance_status`，并投影 `hasArmingFunc`、`cameraType` 及目标接口实际使用的其他静态过滤字段。
- 使用 `SECURITY_LIST_SPACE` 维护项目级空间范围，至少持久化 `id`、`local_name`、`building_id`、`floor_id`、`monitoring_surveillance_status`。
- 使用 `cache_object_rel` 维护设备到空间关系：`SPIACU` 服务空间使用 `service_space`，摄像头和门禁所在空间使用 `in_space`。关系方向、项目隔离和去重遵循统一缓存对象模型契约。
- 设备静态接口在同一响应中继续提供前端现有消费的模型图字段（`bimId`、`bimLocation`、`outline`）；本期不新增独立模型图请求或前端合并流程。
- 缓存初始化与 DTX/RWD 变更刷新必须覆盖对象、扩展字段、Key 关联和关系；写入完成前不得把安防缓存标记为 READY。

### R2. 静态读取链路切换

- `/security/function` 从设备缓存判断通行、撤布防和空间安防功能是否存在。
- `/security/listDeploymentSpace` 从设备缓存、`service_space`/`in_space` 关系及空间缓存组装结果。
- `/security/listAccessSpace` 从设备缓存和 `in_space` 关系取得候选空间，规则存在性继续实时查询。
- `/security/listEquipment` 从设备缓存取得 12 类安防设备，避免再次远程拉取并全量过滤。
- `/security/dashboard` 从空间、设备及关系缓存取得受管控空间、设备、楼层和建筑关联。
- 设备统计从设备缓存取得 6 类统计对象，只对实际参与统计的去重 ID 查询动态状态。

### R3. 缓存就绪与降级

- 每条静态读取链路先按项目检查 `RunningMonitorTypeEnum.SECURITY` 的缓存状态。
- READY 时不得再调用 RWD 查询同一批静态对象、建筑楼层或设备-空间关系。
- 未 READY 时整条逻辑使用原有 RWD 路径，不能在一次业务计算中混用不完整缓存和 RWD 结果。
- 数据库或反序列化异常不得伪装成缓存未命中；只允许基于明确的未就绪状态降级。

### R4. 需求配置结果接入

- 将安防 `ARMING`、`PASSING` 模板纳入 `demand_config_result` 的支持、刷新和初始化范围，复用现有结果表、快照和查询契约，不另建平行缓存。
- `/security/dashboard` 和安防页面需求配置查询优先使用 `listConfigResultsByInstances`，避免每次请求执行全量实例填充和四级继承计算。
- 结果读取必须保留屏蔽、黑名单、自定义配置、模板日期及继承后的最终配置语义。
- 安防需求结果读取与空调保持一致，不增加独立 READY 状态；结果 miss 按无配置处理，不回退实时继承，也不以结果表是否为空推断就绪。

### R5. 动态数据与计算优化

- `deploymentStatus`、`accessDemand`、`equipOnSet`、`onlineStatus`、`orderFailAlarm`、`armingStatus`、`runStatus`、`deployWithdrawAlarmStatus` 等动态值或规则不得写入静态对象缓存。
- 通行空间过滤改为基于设备规则集合和设备-空间关系的一次聚合，避免空间乘设备的重复遍历，并移除循环内逐空间 INFO 日志。
- 设备统计只查询目标设备和目标信息点。若需要拆批或并行，必须使用有界并发并通过压测证明收益；异常事件查询可继续并行，但各异步任务先构造独立结果，汇合后再合并共享状态。
- `/running-monitor/batchQueryInfoCode`、异常事件列表、异常状态和 IoT 数据变更 SSE 继续返回实时数据，不改变业务过滤规则。

### R6. 可观测性与性能验证

- 保留或补齐目标接口的分阶段耗时，至少区分静态对象、关系、需求配置、IoT、异常事件和内存聚合。
- 性能报告记录数据规模、缓存状态、预热次数、样本数、P50/P95、总耗时及主要阶段耗时，并与本 PRD 基线对照。
- 性能优化不得以延长数据刷新周期、吞掉异常或改变返回数据为代价。

## 验收标准

- [ ] AC1：为 `/security/function`、`/security/dashboard`、`/security/listDeploymentSpace`、`/security/listAccessSpace`、`/security/listEquipment`、`/security/equipment/statistics` 建立优化前后结果对照；相同输入下字段、业务分组和对象集合一致，非契约化顺序可忽略。
- [ ] AC2：缓存 READY 时，通过 mock/spy 或调用计数证明目标读取路径不再访问 RWD 静态对象、建筑楼层和设备-空间关系；未 READY 时原路径结果正确。
- [ ] AC3：初始化或增量刷新后，设备、空间、扩展字段和两类关系按项目准确替换；删除和关系迁移不会残留旧数据，READY 只在完整成功后写入。
- [ ] AC4：`ARMING`、`PASSING` 的预计算结果与原实时继承结果对照一致；读取不依赖 READY 状态，结果 miss 按无配置处理且不回退实时继承。
- [ ] AC5：通行空间满足“两类规则均存在”的业务口径，规则变化在下一次实时查询中可见；设备统计、异常状态和批量 IoT 查询不会返回静态旧值。
- [ ] AC6：设备接口一次返回普通静态字段和前端现有消费的模型图字段；不增加额外模型图请求或前端二次合并，有 BIM 位置的设备集合与改造前一致。
- [ ] AC7：聚焦单元测试覆盖缓存 READY/未 READY、空集合、重复关系、部分关系缺失、规则分属不同门禁、IoT 值缺失及异步查询异常；受影响模块编译和测试通过。
- [ ] AC8：使用统一压测方法提交优化前后报告；5 条有基线的慢接口均不得回退，最终性能门槛按“待确认项”补充后执行验收。

## 非目标

- 不重建 `cache_object`、`cache_object_rel`、`cache_object_model_view` 或 `demand_config_result` 的通用底层模型。
- 不改变屏幕配置、视频编组增删改查、异常事件风控目标或 SSE 调度频率。
- 不将 IoT 信息点、孪生规则或异常事件持久化为安防静态缓存。
- 不在本期拆分模型图字段接口或改变前端现有模型图消费方式；底层已有模型视图存储仅作为内部实现细节。
- 不在本任务中顺带优化安防以外的运行监测模块。
- 本仓库不直接修改未纳入当前工作区的前端代码；后端保持现有设备接口返回契约并与前端联调确认字段完整。

## 风险与依赖

- 安防缓存初始化必须同时覆盖云端、边缘端实际运行路径以及云边同步发布顺序。
- 需求结果表当前已有业务类型边界；扩展到安防时需验证刷新事件、历史快照和结果 miss 语义不会影响已支持类型。
- IoT 查询是最高耗时点，实际收益依赖数据规模和底层查询实现，不能仅靠调用方并发掩盖数据库或缓存索引问题。
- 模型图字段与设备数据一并返回会增加响应体积，需在性能复测中同时关注响应大小和序列化耗时；若底层模型视图独立存储，需保证服务端组装不引入 N+1。

## 发布前环境验证

- 使用同一项目快照对 6 个安防接口执行优化前后结构化响应对照。
- 串行验证 SECURITY 静态缓存未 READY、READY 冷读和 READY 预热；需求配置在三种场景均读取预计算结果。
- 修改测试设备动态状态或规则后，验证下一次 HTTP/SSE 查询立即返回新值。
- 记录 P50/P95、阶段耗时、调用次数、响应体大小和错误率。
- 环境验证开始前由发布负责人确定固定接口 SLA，或相同环境相对基线的 P50/P95 降幅。
