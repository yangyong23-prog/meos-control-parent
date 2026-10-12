# 通风需求保存后非通风空间生成配置结果修复 - 实现计划

## 改动清单（按序执行）

### 1. 改造 `refreshInstances`（`DemandConfigResultRefreshRepositoryImpl.java:330-365`）

- 方法开头调用 `filterInstancesByDemandSpaces(templateId, instances)` 得到过滤后的实例集合。
- 过滤后集合为空时直接 return（不执行稳定计算与差集同步）。
- 后续 `toInstanceTypeAndInstanceIds`、`listStableConfigsByInstances`、`toUpsertParams`、`toMissingDeleteParams`、`syncResults` 全部基于过滤后的集合。
- 保持现有 `PerformanceTracker` 记录与异常日志结构不变（本方法当前无 tracker，无需新增）。

### 2. 新增私有方法 `filterInstancesByDemandSpaces`（实例级刷新的统一需求空间过滤入口）

```java
private @NonNull Set<DemandInstanceValueObject> filterInstancesByDemandSpaces(
        @NonNull Long templateId,
        @NonNull Collection<DemandInstanceValueObject> instances)
```

- 从 `instances` 中收集 SPACE / FLOOR / BUILDING 实例；三种都没有（如仅 PROJECT）时直接返回原集合，避免无意义的需求空间查询。
- 通过 `this.queryTemplate(templateId).getDemandTemplateType()` 获取 `templateType`，调用 `listDemandSpaces(templateType)` 查询全项目需求空间。
- 过滤规则（逐实例判断）：
  - SPACE：仅保留在全项目需求空间集合中的空间；
  - FLOOR：仅保留需求空间关联楼层（非空 `floorId` 推导）；
  - BUILDING：仅保留需求空间关联建筑（非空 `buildingId` 推导）；
  - PROJECT：保留。
- 需求空间为空时，SPACE/FLOOR/BUILDING 全部剔除，仅保留 PROJECT 等上级实例。
- 该方法不处理黑名单/屏蔽：仍由稳定链路 `removeInvalidInstances` 与屏蔽事件监听器负责。

### 3. 新增私有方法 `listDemandSpaces`

```java
private @NonNull List<DtpRwdSpaceDTO> listDemandSpaces(
        @NonNull DemandTemplateTypeEnum templateType)
```

- 构造 `DemandSpacesQueryParam(templateType)`，`addWithColumn(id, floorId, buildingId)`，不设范围/黑名单/屏蔽过滤，调用 `templateType.toDemandTypeEnum().demandStrategy().listDemandSpace(queryParam)` 返回全项目需求空间。
- 采用全项目口径的原因：通风策略经 `BaseRunningMonitorStrategy` 委托仅支持单实例过滤且 SPACE 不生效，照明/安防策略行为各自独立，按范围过滤会与特定策略实现耦合，违背预计算通用功能要求。

### 4. 其余方法保持原样

- `refreshByInstanceAndChildren` / `expandInstancesAndChildren`：物理展开逻辑不改动（非需求空间在 `refreshInstances` 统一入口被剔除）。
- `refreshByDtxChangedInstances` / `refreshInstance`：不改动，自动受统一入口保护。
- `InstanceService instanceService` 字段保留（物理展开仍使用）。

## 验证命令

```bash
# 编译 common 模块
mvn -pl common -am compile -DskipTests

# 检查问题
# （IDE GetProblems / 编译错误）
```

## 行为验证（联调/测试环境，配合测试）

1. 保存项目级通风配置 → 查询 `demand_config_result`：该通风模板类型下 SPACE 记录仅含通风需求空间（有通风设备服务且功能类型匹配）。
2. 保存建筑/楼层级通风配置 → 结果表仅写入该范围内真正的通风需求空间及其关联楼层/建筑层级结果。
3. 保存空间级通风配置 → 仅刷新该空间，行为不回归。
4. 保存空调配置 → 事件刷新行为不回归（空调需求空间口径不变）。
5. 黑名单移出空间 → 刷新仍正常写入该空间结果（统一入口不误伤黑名单移出路径）。
6. 事件刷新与定时任务全量刷新的 SPACE 口径一致（可分别触发后对比结果表 SPACE 集合）。

## 风险文件与回滚点

- 唯一改动文件：`common/.../structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java`
- 回滚：`git checkout -- <file>` 或 revert 对应提交。
- 无数据库迁移、无接口契约变化。

## 完成前检查（Phase 3）

- [ ] 编译通过（common 模块）
- [ ] `trellis-check` 质量校验
- [ ] 提交前用 meos-commit-message 规范生成提交信息并与用户确认
