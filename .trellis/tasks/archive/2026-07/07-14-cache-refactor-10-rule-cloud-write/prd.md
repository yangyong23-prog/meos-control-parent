# IOT 规则云端写入

## Goal

将末端、照明的云端规则事件接入去重规则主表和 Key 归属关联，不再维护旧版持久化规则缓存。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01 至 09 已通过，直接依赖子任务 07、08。

## Requirements

- `AIR_TERMINAL`、`LIGHTING` 只写新版 `cache_rule` 与
  `cache_rule_key_rel`；其他运行监测系统继续使用旧规则表。
- 新版写入先按 `(projectId, objectId, infoCode, type)` 匹配或生成稳定规则 ID，
  UPSERT 主表后再替换当前 Key 关联。
- 云端规则类型保持 `PERSISTENCE`，边缘同步后的业务读取保持 `TWIN`。
- Key 关联只表达单 Key 写入归属，不保存运行监测类型或业务场景字段。
- 删除模板 Key 时先删除该 Key 关联，再清理不再被任何 Key 引用的孤儿规则；
  不得影响其他 Key 共享的规则。
- 规则准备和业务校验在删除旧关联前完成；事务失败不得留下悬空关联。
- 未 READY 的末端、照明规则消费者走实时查询，不以旧规则表作为回退缓存。

## Out Of Scope

- 不切换规则读取，不实现初始化、READY 或对象消费者。
- 不删除旧规则表；旧表仍服务未迁移的其他运行监测系统。

## Acceptance Criteria

- [x] 末端、照明规则事件只维护新版规则模型并按业务键去重。
- [x] 主表 UPSERT 先于 Key 关联替换，异常时不会产生悬空关联。
- [x] 单 Key 重建或删除不破坏其他 Key 的共享规则，最后一个引用删除后可清理孤儿。
- [x] 其他系统旧规则写入保持不变。
- [x] 未 READY 的支持类型使用实时规则查询，不依赖旧持久化规则缓存。

## Review Gate

最终写入和读取边界已在集成任务复核通过，本轮归档。
