## 1. 默认值变更

- [x] 1.1 修改 ShellConfigItemEnum.java 中8个FREQ_RANGE枚举的defaultValues从"35,40"改为"40,50"（ALG_AIR_COOLING_PUMP_1_FREQ_RANGE、ALG_AIR_COOLING_PUMP_2_FREQ_RANGE、ALG_AIR_COOLING_PUMP_ACOPC_FREQ_RANGE、ALG_AIR_COOLING_PUMP_ACOTC_FREQ_RANGE、ALG_COOLING_PUMP_1_FREQ_RANGE、ALG_COOLING_PUMP_2_FREQ_RANGE、ALG_COOLING_PUMP_ACOPC_FREQ_RANGE、ALG_COOLING_PUMP_ACOTC_FREQ_RANGE）

## 2. BDTP同步方法实现

- [x] 2.1 在 InnerSchdule.java 中新增 `saveFreqRangeConf()` 私有方法，查询SCENE_ALG场景下GROUP_ALG_ACAC_FREQ和GROUP_ALG_ACCC_FREQ分组的壳配置
- [x] 2.2 在 `saveFreqRangeConf()` 中遍历壳配置项，匹配8个FREQ_RANGE枚举码，将范围值按逗号拆分为vFDMinFreqCtrl和vFDMaxFreqCtrl两条推送记录
- [x] 2.3 实现空值处理逻辑：value为空时推送paramValue=""，objectId为空时跳过
- [x] 2.4 实现值格式校验：split后长度不足2时记录WARN日志并跳过
- [x] 2.5 调用 dtpTwinClientService.saveList() 推送数据，添加异常处理和日志记录

## 3. 定时任务集成

- [x] 3.1 在 synShellConfigToTwin() 方法中添加 this.saveFreqRangeConf() 调用，位于现有同步方法之后

## 4. 编译验证

- [x] 4.1 执行Maven编译确认无编译错误
