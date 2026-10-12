## ADDED Requirements

### Requirement: 屏蔽所有控制指令同步
边缘端定时任务必须将空调冷源（ACAC）和冷源（ACCC）系统中的"屏蔽所有控制指令"壳配置参数同步至BDTP孪生服务，调用 `/twc/revise/reviseObjectInSys` 接口。

#### Scenario: 空调冷源配置值为true时同步
- **WHEN** 定时任务执行且空调冷源（ACAC）系统的 `ALG_AIR_COOLING_SHIELD_ALL_ENABLED` 配置值为"true"
- **THEN** 调用 `reviseObjectInSys` 接口，传入系统ID和revise=1（表示屏蔽）

#### Scenario: 空调冷源配置值为false时同步
- **WHEN** 定时任务执行且空调冷源（ACAC）系统的 `ALG_AIR_COOLING_SHIELD_ALL_ENABLED` 配置值为"false"
- **THEN** 调用 `reviseObjectInSys` 接口，传入系统ID和revise=0（表示不屏蔽）

#### Scenario: 冷源配置值不为空时同步
- **WHEN** 定时任务执行且冷源（ACCC）系统的 `ALG_COOLING_SHIELD_ALL_ENABLED` 配置值不为空
- **THEN** 调用 `reviseObjectInSys` 接口，根据配置值转换revise参数（true→1, false→0）

#### Scenario: 配置值为空时不推送
- **WHEN** 定时任务执行且系统对应的配置值为空
- **THEN** 不调用 `reviseObjectInSys` 接口，跳过该系统的同步

### Requirement: 屏蔽指令数据格式
同步至BDTP的屏蔽控制指令数据必须符合以下JSON格式：
```json
{
  "objectId": "系统ID",
  "revise": 1
}
```

#### Scenario: 配置值转换为revise参数
- **WHEN** 壳配置值为"true"
- **THEN** 转换revise=1

#### Scenario: 配置值转换为revise参数
- **WHEN** 壳配置值为"false"
- **THEN** 转换revise=0

### Requirement: 异常处理和日志记录
同步过程中必须捕获并记录异常，确保单次同步失败不影响其他同步任务执行。

#### Scenario: BDTP接口调用失败
- **WHEN** `reviseObjectInSys` 接口调用失败（网络异常、服务不可用等）
- **THEN** 记录ERROR级别日志，包含异常堆栈，继续执行后续同步任务

#### Scenario: 配置值格式异常
- **WHEN** 壳配置值不是标准的"true"或"false"字符串
- **THEN** 记录WARN级别日志，跳过该配置项的同步，继续处理其他系统
