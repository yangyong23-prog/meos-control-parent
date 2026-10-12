## ADDED Requirements

### Requirement: 频率调节范围参数BDTP同步
边缘端定时任务必须将空调冷源（ACAC）和冷站（ACCC）的8个"频率调节范围"壳配置参数同步至BDTP孪生服务，调用 `/twc/conf/saveList` 接口。

#### Scenario: 壳配置值不为空时拆分推送
- **WHEN** 定时任务执行且某个FREQ_RANGE壳配置项的value不为空（如"40,50"）且objectId不为空
- **THEN** 将值按逗号拆分为min和max，生成两条BDTP推送记录：`{objectId: item.getObjectId(), paramCode: "vFDMinFreqCtrl", paramValue: min, paramLabel: item.getName()}` 和 `{objectId: item.getObjectId(), paramCode: "vFDMaxFreqCtrl", paramValue: max, paramLabel: item.getName()}`，并调用 `saveList` 推送

#### Scenario: 壳配置值为空时推送空值
- **WHEN** 定时任务执行且某个FREQ_RANGE壳配置项的value为空但objectId不为空
- **THEN** 生成两条BDTP推送记录：`{objectId: item.getObjectId(), paramCode: "vFDMinFreqCtrl", paramValue: "", paramLabel: item.getName()}` 和 `{objectId: item.getObjectId(), paramCode: "vFDMaxFreqCtrl", paramValue: "", paramLabel: item.getName()}`，并调用 `saveList` 推送

#### Scenario: objectId为空时跳过推送
- **WHEN** 定时任务执行且某个FREQ_RANGE壳配置项的objectId为空
- **THEN** 跳过该条记录，不加入推送列表

### Requirement: 覆盖9个FREQ_RANGE壳配置枚举
同步必须覆盖以下9个壳配置枚举项：
- ACAC系统：ALG_AIR_COOLING_PUMP_1_FREQ_RANGE、ALG_AIR_COOLING_PUMP_2_FREQ_RANGE、ALG_AIR_COOLING_PUMP_ACOPC_FREQ_RANGE、ALG_AIR_COOLING_PUMP_ACOTC_FREQ_RANGE、ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE
- ACCC系统：ALG_COOLING_PUMP_1_FREQ_RANGE、ALG_COOLING_PUMP_2_FREQ_RANGE、ALG_COOLING_PUMP_ACOPC_FREQ_RANGE、ALG_COOLING_PUMP_ACOTC_FREQ_RANGE

#### Scenario: ACAC系统的频率调节范围推送
- **WHEN** 定时任务执行且查询到GROUP_ALG_ACAC_FREQ分组下的壳配置数据
- **THEN** 将5个ACAC频率调节范围参数（含ACPPC）按控制组实例拆分后推送至BDTP

#### Scenario: ACCC系统的频率调节范围推送
- **WHEN** 定时任务执行且查询到GROUP_ALG_ACCC_FREQ分组下的壳配置数据
- **THEN** 将4个ACCC频率调节范围参数按控制组实例拆分后推送至BDTP

### Requirement: 频率调节范围数据格式
同步至BDTP的频率调节范围数据必须符合以下JSON格式：
```json
[
  {
    "objectId": "控制组ID",
    "paramCode": "vFDMinFreqCtrl",
    "paramValue": "下限值",
    "paramLabel": "频率调节范围"
  },
  {
    "objectId": "控制组ID",
    "paramCode": "vFDMaxFreqCtrl",
    "paramValue": "上限值",
    "paramLabel": "频率调节范围"
  }
]
```

#### Scenario: 范围值正确拆分
- **WHEN** 壳配置值为 "40,50"
- **THEN** 生成 vFDMinFreqCtrl 的 paramValue 为 "40"，vFDMaxFreqCtrl 的 paramValue 为 "50"

#### Scenario: 值格式异常时跳过并记录日志
- **WHEN** 壳配置值无法按逗号正确拆分为两部分（如格式异常或split后长度不足2）
- **THEN** 跳过该条记录，记录WARN级别日志，继续处理其他配置项

### Requirement: 默认值变更
8个FREQ_RANGE枚举的defaultValues必须从 "35,40" 变更为 "40,50"。

#### Scenario: 新项目使用新默认值
- **WHEN** 新项目首次加载壳配置且未保存过FREQ_RANGE参数
- **THEN** 前端显示的默认值为 "40,50"

### Requirement: 定时任务集成
`saveFreqRangeConf()` 方法必须在 `InnerSchdule.synShellConfigToTwin()` 定时任务中被调用，与其他同步方法保持相同的调度周期（每5分钟）。

#### Scenario: 定时任务包含频率范围推送
- **WHEN** synShellConfigToTwin 定时任务执行
- **THEN** 在现有同步方法之后调用 saveFreqRangeConf()

### Requirement: 异常处理和日志记录
同步过程中必须捕获并记录异常，确保单次同步失败不影响其他同步任务执行。

#### Scenario: BDTP接口调用失败
- **WHEN** saveList 接口调用失败（网络异常、服务不可用等）
- **THEN** 记录ERROR级别日志，包含异常堆栈，不影响其他同步任务的执行

#### Scenario: 壳配置查询异常
- **WHEN** 查询壳配置时发生异常
- **THEN** 记录ERROR级别日志，方法安全退出，不影响其他同步任务
