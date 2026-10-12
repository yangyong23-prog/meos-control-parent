# rcRunStatus 计算日志规范

## 规范说明

本规范定义 rcRunStatus 计算过程中的日志记录要求，确保线上问题可追溯、可排查。

## 需求

### 需求 1：系统必须记录 rcRunStatus 计算的完整上下文

**优先级**：P0

**描述**：
每次 rcRunStatus 计算时，必须记录以下信息：

- 设备 ID（objectId）
- 触发计算的信息点（infoCode）及其值
- 从缓存读取的输入值（onlineStatus、runStatus 等）
- 决策路径和原因
- 最终计算结果

**验证方法**：

- 日志中包含所有必要字段
- 可以通过设备 ID 检索到完整的计算历史

### 需求 2：系统必须使用统一的日志格式

**优先级**：P0

**描述**：
所有 rcRunStatus 相关日志必须使用统一的前缀和格式：

```
[rcRunStatus-场景] 字段1:值1 | 字段2:值2 | ...
```

场景类型：

- `[rcRunStatus-计算]`：正常计算流程
- `[rcRunStatus-公式计算]`：表达式模式计算
- `[rcRunStatus-跳过]`：跳过计算
- `[rcRunStatus-异常]`：计算异常

**验证方法**：

- 所有日志可以通过 `grep "rcRunStatus"` 检索
- 日志格式一致，便于自动化解析

### 需求 3：系统必须记录控制指令模式下的计算日志

**优先级**：P0

**描述**：
在 `checkControlRealStatus()` 方法中，必须记录：

- 触发信息点和值
- 数据状态（1=完成，2=进行中）
- 缓存读取的 onlineStatus 和 runStatus
- 决策路径（进行中判断、在线状态检查等）
- 最终结果

**验证方法**：

- 每次调用都记录日志
- 日志包含完整的决策路径说明

### 需求 4：系统必须记录表达式模式下的计算日志

**优先级**：P1

**描述**：
在 `getTriggerFormulaInfoCodeValue()` 方法中（sourceIotType == 1），必须记录：

- 触发信息点和值
- 公式变量的实际值
- 公式执行结果

**验证方法**：

- 公式计算完成后记录日志
- 变量值以 JSON 格式记录

### 需求 5：系统必须记录跳过计算的原因

**优先级**：P1

**描述**：
当 `triggerFormulaInfo == null` 时，必须记录：

- 设备 ID
- 触发信息点
- 跳过原因（无配置、设备特殊逻辑等）
- 说明信息

**验证方法**：

- 使用 WARN 级别记录
- 原因说明清晰明确

### 需求 6：系统必须记录计算异常

**优先级**：P0

**描述**：
任何计算异常必须记录：

- 设备 ID
- 触发信息点和值
- 方法名
- 异常信息
- 异常堆栈

**验证方法**：

- 使用 ERROR 级别记录
- 包含完整异常堆栈

### 需求 7：日志量必须可控

**优先级**：P1

**描述**：
日志增强不得显著增加系统负担：

- 只有配置了 triggerFormulaInfo 的设备才记录日志
- 单条日志大小 < 500 字节
- 每设备每次上报最多 1-2 条日志

**验证方法**：

- 日志增量 < 10KB/分钟
- 性能测试无明显影响

## 日志格式规范

### 状态值中文映射

所有日志中的状态值必须附带中文说明：

| 状态值 | 中文名称 |
|-----|------|
| -1  | 无效   |
| 0   | 停止   |
| 1   | 运行   |
| 2   | 开启中  |
| 3   | 关闭中  |

### 计算日志格式

```
[rcRunStatus-计算] 设备:{objectId} | 触发:{infoCode}={value} | 数据状态:{status} | 缓存:{key1}={value1}({name1}), {key2}={value2}({name2}) | 决策:{reason} | 结果:{result}({resultName})
```

**字段说明**：

- `设备`：设备 ID
- `触发`：触发计算的信息点及其值
- `数据状态`：进行中 / 完成
- `缓存`：从 Redis 读取的变量值（附带中文名称）
- `决策`：决策路径说明（使用简洁中文）
- `结果`：最终计算的 rcRunStatus 值（附带中文名称）

**示例**：

```
[rcRunStatus-计算] 设备:DEV001 | 触发:equipSwitchSet=1 | 数据状态:进行中 | 缓存:onlineStatus=1, runStatus=0(停止) | 决策:进行中→直接返回 | 结果:2(开启中)
[rcRunStatus-计算] 设备:DEV002 | 触发:equipSwitchSet=0 | 数据状态:完成 | 缓存:onlineStatus=1, runStatus=1(运行) | 决策:设定关闭但实际运行→关闭中 | 结果:3(关闭中)
```

### 公式计算日志格式

```
[rcRunStatus-公式计算] 设备:{objectId} | 触发:{infoCode}={value} | 变量:{json} | 公式结果:{result}({resultName})
```

**示例**：

```
[rcRunStatus-公式计算] 设备:DEV001 | 触发:equipSwitchSet=1 | 变量:{onlineStatus=1, equipSwitchSet=1, runStatus=0} | 公式结果:2(开启中)
```

### 跳过日志格式

```
[rcRunStatus-跳过] 设备:{objectId} | 触发:{infoCode} | 原因:{reason} | 说明:{description}
```

### 异常日志格式

```
[rcRunStatus-异常] 设备:{objectId} | 触发:{infoCode}={value} | 方法:{methodName} | 异常:{message}
```

## 状态值说明

| 状态值 | 状态名称 | 说明           |
|-----|------|--------------|
| -1  | 无效   | 设备离线或数据缺失    |
| 0   | 停止   | 设备已停止运行      |
| 1   | 运行   | 设备正常运行       |
| 2   | 开启中  | 设备正在启动（过渡状态） |
| 3   | 关闭中  | 设备正在停止（过渡状态） |

## 日志检索示例

### 查看设备计算历史

```bash
grep "设备:DEV001" control.log | grep "rcRunStatus"
```

### 查找异常

```bash
grep "\[rcRunStatus-异常\]" control.log
```

### 统计状态分布

```bash
grep "设备:DEV001" control.log | grep "\[rcRunStatus-计算\]" | \
  grep -o "结果:[0-9-]*" | sort | uniq -c
```

## 实现约束

- 使用 `ControlLogUtil` 进行日志记录
- 不得修改现有 XML 日志配置
- 日志级别：INFO（正常计算）、WARN（跳过）、ERROR（异常）
- 支持通过 Nacos 动态调整日志级别（可选）
