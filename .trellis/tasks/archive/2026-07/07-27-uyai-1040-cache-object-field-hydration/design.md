# Design

## Data Flow

```text
业务范围查询（classCode 完整字段） -> 公共写服务直接投影 object_data/view_data
                                  -> 单 Key 短事务写对象、视图、Key 和关系
```

## Decisions

- 保留 `replaceModelCacheKey(...)` 现有完整快照契约，公共写服务不新增对象 RWD 查询。
- 单 classCode 入口直接使用 `CacheObjectExtensionFieldConfig.listRwdQueryFields(classCode)`；末端混合设备类入口在一次范围查询前计算目标 classCode 字段并集。
- 使用范围查询实际返回对象作为本次 Key 的最终范围；不增加业务层分片、重试或远程结果完整性校验。
- 两份 JSON 仍分别按白名单投影。完整快照覆盖能正确清除已删除字段，避免历史 JSON merge 留下脏字段。
- 照明关系服务的关系目标查询已经按目标 classCode 请求完整字段，且与关系查询合并为同一次远程调用，本次不拆成额外补全查询。

## Compatibility

- 空 Key、事务边界、READY 时序和关系替换范围不变。
- 照明范围查询仍保留 `serviceAreaType`、`funcType`、监控状态、BIM 字段以及服务空间/楼层关系所需字段。
- 旧版 RWD 缓存写入路径不受影响。
