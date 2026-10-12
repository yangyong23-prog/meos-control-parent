# 设计文档：客流出入口统计新增客流人数

## 1. 现状与数据流

`getPassagewayHeadCount()`（common 模块，云/边共用，也被 2 个 SSE 策略复用）返回
`List<PassengerFlowHomeStatistics>`（项目行首位 + 门组行，均经 accm in/out 实例化规则并集过滤）：

```
每行 PassengerFlowHomeStatistics:
  objectId / objectName
  inPeopleFlow   ← listInfoCodeValueInOneHour(accmInPeopleFlow) 最新时刻值（有记录才 set，缺失为 null）
  outPeopleFlow  ← 同上 accmOutPeopleFlow
  graph[24]      ← queryObjectIdPersistentDataMap(HOUR, accdIn/accdOut) 当日累计差分每小时持久化值
                     TendencyFigure(spInPeopleFlow, spOutPeopleFlow, time)；无数据小时点为 null
```

组装集中在静态方法 `PassengerFlowHomeStatistics.iotToStatistics(...)`。

## 2. 契约变更（纯增量，无破坏）

### 2.1 统计行

```json
{
  "objectId": "...", "objectName": "项目",
  "inPeopleFlow": 1000, "outPeopleFlow": 900,
  "netPeopleFlow": 100,
  "graph": [ ... ]
}
```

`netPeopleFlow`（Long，可空）= `inPeopleFlow − outPeopleFlow`；两侧均有值才计算，任一为 null 则 null。

### 2.2 图表点

```json
{ "spInPeopleFlow": 500, "spOutPeopleFlow": 460, "spNetPeopleFlow": 40, "time": "2026-09-09 10:00:00" }
```

`spNetPeopleFlow`（Long，可空）= 该点 `spInPeopleFlow − spOutPeopleFlow`（accd 当日口径，数据源不变）；任一侧为 null 则 null。

## 3. 实现要点

- 差值计算必须在 in/out **全部填充完成后**进行：
  - 统计行：`energyDataResponses.forEach(...)` 之后判断（forEach 内信息点顺序不定，不可在 set 时即时计算）。
  - 图表点：`infoCodeHoverMap.forEach(...)` 填完 `timeMap` 后统一遍历一次 `timeMap.values()` 计算 `spNetPeopleFlow`。
- 转换失败按现有约定降级为 0（`bigDecimalToLong` 已有行为，视为有值 0），差值字段对 0 正常参与计算；本任务不改变该约定。
- 字段放置与构造器：
  - `PassengerFlowHomeStatistics`：`netPeopleFlow` 放在 `outPeopleFlow` 与 `graph` 之间（无参构造使用，无影响）。
  - `TendencyFigure`：`spNetPeopleFlow` 放在 `spOutPeopleFlow` 与 `time` 之间；`@AllArgsConstructor` 参数顺序变化，同步更新唯一调用点 `new TendencyFigure(null, null, startOfDay)`（PassengerFlowHomeStatistics.java:77）为 4 参（net 先传 null，由计算填充）。

## 4. 边界情况

| 场景 | 行为 |
|---|---|
| in 有值、out 无记录（仅 in 有规则/仅单侧上报） | `netPeopleFlow = null`（前端 `--`） |
| in/out 均无记录 | 两字段 null，`netPeopleFlow = null` |
| in/out 均有值（含 0） | 差值 in − out |
| 某小时点无 accd 数据（初始化点位） | `spNetPeopleFlow = null`，其余小时点正常 |
| 无有效对象（规则过滤后为空） | 返回空列表，行为同现状 |

## 5. 改动清单

| 文件 | 改动 |
|---|---|
| `common/.../model/vo/passengerflow/PassengerFlowHomeStatistics.java` | 新增 `netPeopleFlow` 字段 + 注释；`iotToStatistics` 统计行差值计算 + `timeMap` 遍历填充 `spNetPeopleFlow`；更新 `new TendencyFigure(...)` 调用点 |
| `common/.../model/vo/passengerflow/TendencyFigure.java` | 新增 `spNetPeopleFlow` 字段 + 注释 |

不改：接口签名、规则过滤、数据源查询（accm moment / accd HOUR）、24 点补齐、SSE 复用链路。

## 6. 验证

1. `common` 模块 `mvn compile` 通过（必要时 saas/local 联动编译）。
2. 静态检查新字段注释与计算逻辑；人工核对 JSON 序列化输出（含 null 场景）。
3. 运行期手工验证（如可用测试环境）：统计行 in/out 双值 → 差值；单侧缺失 → null；图表有数据小时差值、空小时 null。
