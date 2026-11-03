# 契约：实时值共享缓存 `apc:iot_real_time_data`

写方：meos-control-server-edge（本仓，B2）。读方：本仓与 dtp-meos（bdtp）。
本文件与 `OptRealtimeContractTest`、`OptRealtimeKeyCompatTest` 一起构成契约；改动它们就是改契约，必须同步 bdtp。

## Key

```
apc:iot_real_time_data:{objectId}:{infoCode}
```

- Redis：redis.paas，db 10；裸 key（不带 `meos-control::` / `dtp-control::` 前缀）。
- 无 TTL。有效性由读方按下面的规则判定。
- `rcRunStatus` 不在本契约内：它是 meos-control 自己计算的点，沿用原来的写法（完整 VO、带 TTL）。

## Value（普通 JSON，无类型信息）

| 字段 | 类型 | 含义 |
|---|---|---|
| `objectId` | string | 对象 id |
| `classCode` | string | 类编码，可能为空 |
| `infoCode` | string | 信息点编码 |
| `dataValue` | string | **Twin 原始值**（未乘换算系数、未按小数位取整）；`"~"` 表示离线 |
| `dataTime` | string | 采集时间 `yyyyMMddHHmmss` |
| `dataIndate` | int | Twin 时间常量（分钟），空或 0 表示不过期 |
| `dataStatus` | int | 1 正常，2 异常（含 Twin 推送时已判定超时无效的帧） |
| `source` | string | `mqtt`（实时推送）或 `twin`（回源回填） |
| `writeTime` | long | 写入时的毫秒时间戳 |

值为空的字段不输出。读方必须忽略未知字段。

## 读方判定“可用”

全部满足才可代替一次 Twin `queryTwinDataOnMoment`：

1. 值存在、`dataValue` 不为空且不是 `"~"`；
2. `dataStatus` 为空或等于 1；
3. `now - writeTime <= 120 秒`（Twin 约 45 秒周期推送一次；超过说明推送中断，应回源）；
4. `dataIndate` 为空或 0，或者 `now <= dataTime + dataIndate 分钟`。

不可用时回源 Twin。换算系数、小数位一律在读侧处理。

## 写方保证

- MQTT 收到反馈值后、任何换算之前写入；`"~"` 写离线标记。
- 写前比较 `dataTime`：更早的帧不覆盖；同一 `dataTime` 同值不重复写。
- 回源 Twin 得到的有效值也会回填（`source=twin`）。

## 兼容规则

只加字段不改字段；要改格式就换 key 名（如 `apc:iot_real_time_data:v2:…`），新旧并存一个版本周期。

## 上线顺序

1. 试点站点打开 `switch.opt.realtimeCacheWrite`（只写）。
2. 打开 `switch.opt.realtimeCacheRead=verify` 跑一周，看 `rt.verify.mismatch / (match + mismatch)` < 0.1%。
3. 切 `switch.opt.realtimeCacheRead=on`。之后再通知 bdtp 让 dtp-meos 切读。
