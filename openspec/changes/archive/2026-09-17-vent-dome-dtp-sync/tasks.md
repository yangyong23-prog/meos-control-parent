## 1. DTP同步方法实现

- [x] 1.1 在 `InnerSchdule.java` 中新增 `saveVentDomeConf()` 私有方法：查询 `SCENE_CONTROL` 场景下 `GROUP_CONTROL_VENT_DOME` 分组的壳配置（复用 `queryShellConfigByScene`，查询为空时记 info 日志并退出）
- [x] 1.2 双层遍历 `ShellConfigVO → ShellConfigGroup → ShellConfigItem` 全部 items，逐项组装 `{objectId: ContextUtil.getProjectId(), paramCode: item.getCode(), paramValue: item.getShellConfigValue()（空值取默认值，空白再兜底空串）, paramLabel: item.getName()}`
- [x] 1.3 推送列表非空时记 info 日志（含推送数据）后调用 `dtpTwinClientService.saveList()`，整体 try/catch 使用 `LogExecutor.project()` 记录 ERROR（异常处理范式仿 `saveFreqRangeConf()`）

## 2. 定时任务集成

- [x] 2.1 在 `synShellConfigToTwin()` 中 `this.saveFreqRangeConf()` 之后追加 `this.saveVentDomeConf()` 调用

## 3. 验证

- [x] 3.1 执行 Maven 编译确认无编译错误（`mvn compile -pl local/meos-control-compute-edge -am -DskipTests`）
- [ ] 3.2 日志/接口验证：确认 `/twc/conf/saveList` 收到 22 条项目级参数，未配置参数为默认值（exhaust_open_temp="45"、judge_mode="1"），objectId 为项目ID
