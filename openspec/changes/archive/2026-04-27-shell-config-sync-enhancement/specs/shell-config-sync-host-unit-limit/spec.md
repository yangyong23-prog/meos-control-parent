## ADDED Requirements

### Requirement: 主机开启台数限制规则同步
边缘端定时任务必须将空调冷热源（ACAI）系统中的"主机开启台数限制规则"壳配置参数同步至BDTP孪生服务，调用 `/twc/conf/saveExclusions` 接口。

#### Scenario: 空调冷热源配置值不为空时同步
- **WHEN** 定时任务执行且空调冷热源（ACAI）系统的 `ALG_AIR_COOLING_HOT_HOST_UNIT_LIMIT_RULE` 配置值不为空
- **THEN** 调用 `saveExclusions` 接口，传入系统ID、paramCode为"equipStartInterval"、paramArray包含选中设备组及其最大运行台数

#### Scenario: 配置值为空时传空数组
- **WHEN** 定时任务执行且系统对应的配置值为空（用户未配置）
- **THEN** 仍调用 `saveExclusions` 接口，但paramArray传空数组`[]`，objectId和paramCode正常传入

### Requirement: 主机台数限制数据格式
同步至BDTP的主机开启台数限制规则数据必须符合以下JSON格式：
```json
{
  "objectId": "系统ID",
  "paramCode": "equipStartInterval",
  "paramLabel": "主机开启台数限制规则",
  "paramArray": [
    {
      "objectIds": ["设备ID1", "设备ID2"],
      "maxRuns": "最大运行数量"
    }
  ]
}
```

#### Scenario: 解析壳配置JSON数组
- **WHEN** 壳配置值为 `[{"selected":["Eq1","Eq2"],"countLimit":"2"}]`
- **THEN** 转换为 `{"objectIds":["Eq1","Eq2"],"maxRuns":"2"}` 并放入paramArray

#### Scenario: 多组台数限制规则
- **WHEN** 壳配置值包含多组台数限制规则（如不同机组类型有不同限制）
- **THEN** 每组规则转换为paramArray中的一个元素

### Requirement: 异常处理和日志记录
同步过程中必须捕获并记录异常，确保单次同步失败不影响其他同步任务执行。

#### Scenario: BDTP接口调用失败
- **WHEN** `saveExclusions` 接口调用失败（网络异常、服务不可用等）
- **THEN** 记录ERROR级别日志，包含异常堆栈，继续执行后续同步任务

#### Scenario: JSON解析异常
- **WHEN** 壳配置值JSON格式错误导致解析失败
- **THEN** 记录ERROR级别日志，跳过该配置项的同步，继续处理其他系统
