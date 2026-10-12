# 静态缓存接入设计

## 边界

新增安防缓存策略/构建入口，复用现有 `cache_object`、`cache_object_key_rel`、`cache_object_rel`、模型视图和状态服务。业务策略通过一个 READY 判定选择缓存或旧 RWD 路径；一次请求选定路径后不隐式穿透。

## 构建流程

1. 批量拉取 12 类设备，投影接口实际消费的公共列、静态扩展字段以及 `bimId`、`bimLocation`。
2. 批量拉取项目空间，保留 `building_id`、`floor_id`、`local_name` 及空间模型视图字段（含 `outline`）。
3. 按关系方向批量拉取 `service_space`、`in_space`，以 `(project, graph, rel, object, relObject)` 去重。
4. 转换全部对象与关系后执行范围替换；失败时保留上一份完整范围并记录可重试错误。
5. 完整成功后标记 SECURITY READY。

模型图若单独存储，服务端按对象 ID 一次批量读取再组装，禁止逐对象查询；设备只合并 `bimId`、`bimLocation`，`outline` 只合并到空间响应。

## 读取映射

| 方法 | 缓存来源 |
|---|---|
| `function` | 设备 classCode 与静态扩展字段 |
| `listDeploymentSpace` | 设备 + `service_space`/`in_space` + 空间（含空间 `outline`） |
| `listEquipment` | 12 类设备及同响应 `bimId`、`bimLocation` |
| dashboard/listAccessSpace 候选 | 设备、空间及关系 ID |

## 回滚

保留现有 RWD 查询实现；关闭 READY 或清除安防缓存状态即可恢复旧路径。写入异常不得删除旧完整范围。
