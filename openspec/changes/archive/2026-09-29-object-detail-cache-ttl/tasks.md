## 1. 缓存声明迁移（LocalCacheConstant）

- [x] 1.1 将 `OBJECT_DETAIL_MAP` 声明类型由 `Map<String, RwdObjectBusinessFeildDTO>` 改为 Caffeine `Cache<String, RwdObjectBusinessFeildDTO>`
- [x] 1.2 初始化改为 `Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build()`，不设置 `maximumSize`
- [x] 1.3 补充 `com.github.benmanes.caffeine.cache.Caffeine`、`com.github.benmanes.caffeine.cache.Cache`、`java.util.concurrent.TimeUnit` 的 import，清理不再使用的 import
- [x] 1.4 更新字段注释，说明该缓存为写入后 2 小时过期的本地缓存

## 2. 调用点适配（LocalRedisCacheHandler）

- [x] 2.1 L113 `getObjectBusinessFeildAndSystemInfo` 中 `OBJECT_DETAIL_MAP.get(objectId)` 改为 `getIfPresent(objectId)`
- [x] 2.2 L208 `getObjectBusinessFeild` 中 `OBJECT_DETAIL_MAP.get(objectId)` 改为 `getIfPresent(objectId)`
- [x] 2.3 确认 L220 `OBJECT_DETAIL_MAP.put(objectId, dto)` 保持不变（Caffeine put 签名一致）
- [x] 2.4 确认富化逻辑（L124~L187 查关系、拼 systemId/systemType、就地改写）未改动，put 后就地 set 语义保留
- [x] 2.5 全仓检索 `OBJECT_DETAIL_MAP` 确认无 entrySet/keySet/values/size/containsKey 等 Map 专有 API 调用残留

## 3. 编译与静态验证

- [x] 3.1 执行 `mvn -pl common compile` 验证 common 模块编译通过
- [x] 3.2 执行受影响下游模块编译（如 `mvn -pl local/meos-control-compute-edge -am compile`）验证调用方无编译问题
- [x] 3.3 确认无新增编译告警（未使用 import、类型不匹配等）

## 4. 行为验证

> 运行时验证，需部署环境执行（真实服务 + bdtp 连接 + 时间流逝或临时缩短 TTL）；代码层逻辑已静态确认，本次实施未勾选，留待部署后验证。

- [ ] 4.1 验证缓存命中路径：2 小时内重复访问同一 objectId 不触发 bdtp 回源（可通过日志或调试断点确认）
- [ ] 4.2 验证过期回源路径：模拟条目过期（或临时缩短 TTL 本地验证）后再次访问触发 bdtp 重查并刷新 localName
- [ ] 4.3 验证就地富化对后续读取可见：首次访问后 `systemId`/`systemType` 被富化，二次访问命中短路（L120）不重复查关系
- [ ] 4.4 验证回源失败降级：bdtp 查询异常时 `getObjectBusinessFeild` 返回 null，`RunningLogHandle` 静默跳过且不写缓存（与迁移前一致）

## 5. 收尾

- [x] 5.1 复核变更范围仅限 `OBJECT_DETAIL_MAP`，未触及 `SYSTEM_INFO` 等其他本地缓存
- [x] 5.2 复核 TTL 固定 2 小时、未引入配置化、未设 maximumSize，与决策一致
- [x] 5.3 `openspec status --change object-detail-cache-ttl` 确认 4 个 artifact 全部 done
