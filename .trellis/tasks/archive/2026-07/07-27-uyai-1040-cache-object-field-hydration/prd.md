# UYAI-1040 缓存对象统一字段查询

## Goal

让共享的 `cache_object` 与 `cache_object_model_view` 始终由按 classCode 配置查询得到的完整 RWD 快照写入，避免末端、照明或模板等不同缓存 Key 使用字段不完整的 DTO 相互覆盖 JSON 和独立列，同时不增加对象补查。

## Requirements

- 新版对象模型调用方在已有范围查询中按 classCode 请求 `CacheObjectExtensionFieldConfig.listRwdQueryFields(...)` 返回的完整存储字段。
- 公共写服务直接使用调用方取得的完整 RWD 快照，不再按对象 ID 发起额外补查。
- `object_data` 与 `view_data` 继续按各自 classCode 配置投影并整份替换，不在 SQL 中合并历史 JSON。
- 空对象范围只清理当前缓存 Key 关联。
- 保持现有单 Key 短事务、关系范围、照明扩展动作和异常传播契约。
- 末端混合设备类查询一次计算全部目标 classCode 的字段并集；单 classCode 查询直接使用对应字段集，不按对象循环计算。
- 以性能和可维护性优先：不新增对象快照 RWD 请求，不新增逐对象校验、重试或分片逻辑。

## Acceptance Criteria

- [x] 末端空间、末端设备、白名单、照明回路和模板新版写入入口都按目标 classCode 请求完整存储字段。
- [x] 同一对象由不同缓存 Key 重复写入时，各入口使用相同 classCode 字段配置生成完整快照。
- [x] 公共写服务不为对象快照额外调用 RWD，仍在事务外完成投影、关系准备并在短事务内替换。
- [x] 空对象范围仍原子替换当前 Key 为空范围。
- [x] 照明业务筛选、BIM 字段转换与关系快照行为保持不变。
- [x] `common` 模块编译通过。

## Notes

- 任务编号：`UYAI-1040`。
- 用户最终选择在已有查询中按 classCode 取全字段，明确不要公共写服务再补查一次。
