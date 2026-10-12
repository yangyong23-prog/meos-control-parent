# vent-dome-dtp-sync Specification

## Purpose

穹顶通风（group_control_vent_dome）22 个项目级壳配置参数至 DTP 孪生服务的同步规范：全量推送、objectId 取当前项目ID、值空时取 defaultValues、复用 `/twc/conf/saveList` 接口。

## Requirements
### Requirement: 穹顶通风参数DTP同步

边缘端定时任务 SHALL 将「穹顶通风」（`group_control_vent_dome`）分组下全部 22 个壳配置参数同步至 DTP 孪生服务，调用 `/twc/conf/saveList` 接口，每条推送记录的 objectId 为当前项目ID。推送数据符合以下格式：

```json
[
  {
    "objectId": "Pj0000001",
    "paramCode": "control_vent_dome_exhaust_open_temp",
    "paramValue": "45",
    "paramLabel": "开启排风机的穹顶温度设定值Ts"
  }
]
```

#### Scenario: 定时任务全量推送22个参数

- **WHEN** 定时任务执行且查询到 scene=control、group=group_control_vent_dome 的壳配置
- **THEN** 组装 22 条推送记录 `{objectId: 项目ID, paramCode: item.code, paramValue: 有效值, paramLabel: item.name}`，并调用 `saveList` 一次性推送

#### Scenario: objectId为项目ID

- **WHEN** 组装任意一条穹顶通风推送记录
- **THEN** objectId SHALL 为当前项目ID（如 "Pj0000001"）
- **AND** SHALL NOT 使用 item.objectId（项目级参数无对象实例）

#### Scenario: 查询结果为空时安全退出

- **WHEN** `queryShellConfigByScene` 返回空
- **THEN** 记录 info 日志并退出，SHALL NOT 调用 `saveList`

### Requirement: 参数值取值规则

推送记录的 paramValue SHALL 取"已保存配置值优先，为空时取 defaultValues"（即 `getShellConfigValue()` 语义），保证 DTP 侧恒收到有效值。

#### Scenario: 已配置参数推送保存值

- **WHEN** 某参数已保存配置值（如 control_vent_dome_exhaust_open_temp=50）
- **THEN** paramValue SHALL 为 "50"

#### Scenario: 未配置参数推送默认值

- **WHEN** 某参数从未保存过配置（value 为空）
- **THEN** paramValue SHALL 为该参数 defaultValues（如 control_vent_dome_exhaust_open_temp 推 "45"）

#### Scenario: 单选项参数推送编码值

- **WHEN** control_vent_dome_off_judge_mode / control_vent_dome_on_judge_mode 未配置
- **THEN** paramValue SHALL 为 "1"（干球温度判断）
- **AND** 已配置时 SHALL 推送已保存的编码（"1"/"2"/"3"）

#### Scenario: 值与默认值均空兜底

- **WHEN** 某参数的 value 与 defaultValues 均为空（枚举定义保证不会发生）
- **THEN** paramValue SHALL 推送空串，保证推送条数恒为 22

### Requirement: paramCode与paramLabel取值

推送记录的 paramCode SHALL 直接使用壳配置 item.code，paramLabel SHALL 使用 item.name，SHALL NOT 拆分、重命名或映射。

#### Scenario: code与label原样使用

- **WHEN** 推送 control_vent_dome_exhaust_open_temp 参数
- **THEN** paramCode SHALL 为 "control_vent_dome_exhaust_open_temp"
- **AND** paramLabel SHALL 为 "开启排风机的穹顶温度设定值Ts"

#### Scenario: 分组内新增参数自动纳入

- **WHEN** group_control_vent_dome 分组未来新增参数枚举
- **THEN** 新增参数 SHALL 自动纳入推送，无需代码改动

### Requirement: 全量推送不做增量比对

同步必须每次全量推送全部 22 个参数，SHALL NOT 引入 Redis 缓存比对或其他增量判断机制。

#### Scenario: 每次执行全量推送

- **WHEN** 定时任务每次执行
- **THEN** SHALL 推送全部 22 条记录，不依赖任何上次推送状态

### Requirement: 定时任务集成

`saveVentDomeConf()` SHALL 挂载于 `InnerSchdule.synShellConfigToTwin()` 定时任务，在 `saveFreqRangeConf()` 之后调用，调度周期 SHALL 为每 5 分钟。

#### Scenario: 定时任务包含穹顶通风推送

- **WHEN** `synShellConfigToTwin` 定时任务执行且 syncShellConfig 开关开启
- **THEN** SHALL 在 `saveFreqRangeConf()` 之后调用 `saveVentDomeConf()`

### Requirement: 异常处理和日志记录

同步过程 SHALL 使用 `LogExecutor.project()` 记录日志，并捕获异常确保单次同步失败不影响其他同步任务执行。

#### Scenario: 推送前记录推送数据

- **WHEN** 组装完成且推送列表非空
- **THEN** SHALL 记录 info 日志（含推送数据），随后调用 `saveList`

#### Scenario: DTP接口调用失败

- **WHEN** `saveList` 调用失败（网络异常、服务不可用等）
- **THEN** SHALL 记录 ERROR 日志（含异常堆栈），不影响其他同步任务的执行

#### Scenario: 壳配置查询异常

- **WHEN** 查询壳配置或组装过程发生异常
- **THEN** SHALL 记录 ERROR 日志，方法安全退出，不影响其他同步任务

