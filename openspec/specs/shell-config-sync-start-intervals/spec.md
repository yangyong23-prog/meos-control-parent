## ADDED Requirements

### Requirement: 冷机间隔开启限制规则同步
边缘端定时任务必须将空调冷源（ACAC）和冷源（ACCC）系统中的"冷机间隔开启限制规则"壳配置参数同步至BDTP孪生服务，调用 `/twc/conf/saveStartIntervals` 接口。

#### Scenario: 空调冷源配置值不为空时同步
- **WHEN** 定时任务执行且空调冷源（ACAC）系统的 `ALG_AIR_COOLING_START_INTERVAL_RULE` 配置值不为空
- **THEN** 调用 `saveStartIntervals` 接口，传入系统ID、paramCode为"equipStartInterval"、paramArray包含选中设备组及其间隔时间

#### Scenario: 冷源配置值不为空时同步
- **WHEN** 定时任务执行且冷源（ACCC）系统的 `ALG_COOLING_START_INTERVAL_RULE` 配置值不为空
- **THEN** 调用 `saveStartIntervals` 接口，传入系统ID、paramCode为"equipStartInterval"、paramArray包含选中设备组及其间隔时间

#### Scenario: 配置值为空时传空数组
- **WHEN** 定时任务执行且系统对应的配置值为空（用户未配置）
- **THEN** 仍调用 `saveStartIntervals` 接口，但paramArray传空数组`[]`，objectId和paramCode正常传入

#### Scenario: 多个系统同时同步
- **WHEN** 项目中同时存在ACAC和ACCC系统且都有间隔开启限制规则配置
- **THEN** 组装包含两个系统参数的列表，一次调用 `saveList` 方法完成批量同步

### Requirement: 间隔开启规则数据格式
同步至BDTP的间隔开启限制规则数据必须符合以下JSON格式：
```json
{
  "objectId": "系统ID",
  "paramCode": "equipStartInterval",
  "paramLabel": "冷机间隔开启限制规则",
  "paramArray": [
    {
      "objectIds": ["设备ID1", "设备ID2"],
      "interval": "间隔时间（分钟）"
    }
  ]
}
```

#### Scenario: 解析壳配置JSON数组
- **WHEN** 壳配置值为 `[{"selected":["Eq1","Eq2"],"interval":"30"}]`
- **THEN** 转换为 `{"objectIds":["Eq1","Eq2"],"interval":"30"}` 并放入paramArray

#### Scenario: 多组间隔规则
- **WHEN** 壳配置值包含多组间隔规则（如不同冷机组有不同间隔时间）
- **THEN** 每组规则转换为paramArray中的一个元素

### Requirement: 异常处理和日志记录
同步过程中必须捕获并记录异常，确保单次同步失败不影响其他同步任务执行。

#### Scenario: BDTP接口调用失败
- **WHEN** `saveStartIntervals` 接口调用失败（网络异常、服务不可用等）
- **THEN** 记录ERROR级别日志，包含异常堆栈，继续执行后续同步任务

#### Scenario: JSON解析异常
- **WHEN** 壳配置值JSON格式错误导致解析失败
- **THEN** 记录ERROR级别日志，跳过该配置项的同步，继续处理其他系统
