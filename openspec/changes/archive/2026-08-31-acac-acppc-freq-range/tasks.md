## 1. 壳配置枚举定义

- [x] 1.1 在 `ShellConfigItemEnum.java` L533 之后新增 `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE` 枚举项，字段值：scene="alg", group="group_alg_acac_freq", tab="空调冷源自动需求计算的条件输入", code="alg_air_cooling_pump_acppc_freq_range", name="频率调节范围", unit="Hz", defaultValues="40,50", step=1, min=0, max=99, hint="范围（0-99）", desc="", valueType=null, valSource=`{"relation":{"classCode":["ACPPC"],"graphCode":"CombineSubset","relCode":"Sy2Eg","relDirection":"from"}}`, editAuth=null

## 2. Tab绑定注册

- [x] 2.1 在 `ShellConfigItemExtendEnum.java` L82 之后新增 `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE("alg_air_cooling_pump_acppc_freq_range","冷热共用水泵控制组")` 绑定

## 3. 实例过滤逻辑

- [x] 3.1 在 `ShellConfigServiceImpl.getInstanceByValSource()` L773 的 else-if 条件中追加 `ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE.getCode().equals(shellConfigItem.getCode())`，与 ACOPC/ACOTC 枚举在同一分支

## 4. BDTP推送集合扩展

- [x] 4.1 在 `InnerSchdule.saveFreqRangeConf()` L939 的 `freqRangeCodes` 集合中追加 `ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE.getCode()`，并将注释从"8个"更新为"9个"

## 5. 验证

- [x] 5.1 编译验证：`mvn compile -pl common -am -DskipTests` 确认无编译错误
