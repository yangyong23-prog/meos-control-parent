# 对象与视图扩展字段投影实施计划

## 1. Immutable Field Configuration

- [x] 将两份对象/模型视图 `classCode -> fields` 配置改为直接声明的 `private static final` 不可变 Map。
- [x] 删除实例配置、Spring 注入、`build...()`、`addFields()` 和测试构造入口。
- [x] 测试扫描两份静态 Map，阻止公共字段冲突并验证深度不可变。
- [x] 固化当前末端、照明首版字段范围。
- [x] 字段配置集中在专门配置类中，配置与投影代码不硬编码数据库表名或列名。
- [x] 新增 `objType -> 模型视图基础字段` 静态 Map，并把 `MODEL_VIEW_EXTENSION_FIELDS` 收敛为纯 classCode 增强项。
- [x] 将 `monitoringSurveillanceStatus` 从对象扩展 Map 移除，加入独立字段黑名单和 RWD 基础查询字段。
- [x] 修改云端迁移，为 `cache_object` 增加可空 JSON 状态码数组列。

## 2. Projection and Rebuild

- [x] 实现基于 `containsKey` 的响应保真投影，保留显式 `null`。
- [x] 对象和模型视图分别投影，空结果稳定输出 `{}`。
- [x] 提供公共字段、对象扩展 JSON、模型视图扩展 JSON 的 DTO 重建入口。
- [x] 重建时过滤映射外字段，并确保公共字段最终覆盖扩展内容。
- [x] 提供后续 RWD 查询需要的公共列与两份扩展字段并集。
- [x] 模型视图投影、重建和查询字段 API 接收 `objType + classCode`，使用基础字段与增强字段并集。
- [x] 补齐公开契约 Javadoc、异常说明和关键投影/覆盖逻辑注释。

## 3. Tests and Validation

- [x] 覆盖 Map 独立性、深度不可变和公共字段冲突。
- [x] 覆盖未配置类、字段缺失、显式值、显式 `null`、映射外字段和空交集。
- [x] 覆盖对象/视图字段隔离、SQL `NULL` 容错和 DTO 重建。
- [x] 覆盖设备/空间基础字段、增强字段、未知 objType 和未知 classCode。
- [x] 覆盖状态信息点不进入扩展 JSON、查询字段包含状态以及多 code 列表重建。
- [x] 运行聚焦测试、`common` 编译和 `git diff --check`。
- [x] 使用 `trellis-check` 完成质量复核。

## 本轮实现记录

- `MODEL_VIEW_BASE_FIELDS` 使用 `DtpRwdObjectTypeEnum.EQUIPMENT/SPACE.value()` 固定设备和空间基础字段，并为 `DtpRwdClassCodeEnum.AC_SPACE.value()` 增加空间基础字段兼容项。
- 两份 classCode Map 均保留 22 个显式登记，`GeneralZone`、`ACZone` 保存 `roomFuncType`；模型视图其余 20 个登记为空增强集合。
- 模型视图投影、重建和 RWD 查询字段均显式接收 `objType + classCode`；未知一侧配置缺失时保留另一侧字段。
- 单元测试补充基础字段并集、增强字段隔离、三种未知编码边界及基础 Map 深度不可变校验。

## Review and Stop Point

实现和验证完成后停止，不启动子任务 03；向用户提交首版字段 Map、投影规则和重建测试供审核。
