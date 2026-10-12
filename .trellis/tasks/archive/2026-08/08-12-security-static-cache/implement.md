# 静态缓存接入实施计划

1. [x] 读取 backend/cache-object 规范，确认缓存 Key、关系编码和 READY API。
2. [x] 固化 12 类设备 `bimId`、`bimLocation` 及空间 `outline` 等模型视图字段的旧响应样本。
3. [x] 实现安防设备/空间对象投影及 `SECURITY_LIST_EQUIPMENT`、`SECURITY_LIST_SPACE` 写入。
4. [x] 实现 `service_space`、`in_space` 批量关系构建、范围替换、删除清理和增量刷新。
5. [x] 接入项目初始化、DTX/RWD 事件和 SECURITY READY 判定。
6. [x] 为 `function`、`listDeploymentSpace`、`listEquipment` 增加 READY/未 READY 路由。
7. [x] 验证设备 `bimId`、`bimLocation` 在同一响应中返回、空间保留 `outline` 且无 N+1 查询。
8. [x] 编写聚焦测试，执行 `mvn -pl common -DskipTests compile`、相关测试和 `git diff --check`。

高风险点：范围替换的原子性、关系方向、项目隔离、缓存异常与 RWD 降级。
