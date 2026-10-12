package com.persagy.apccontrol.basic.service.shellconfig;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.entity.ShellConfigGroup;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigGroupEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.outside.dto.ControlObjectDto;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 壳配置场景组装性能回归测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
class ShellConfigServiceImplTest {

    /**
     * 测试集团编码。
     *
     * @since v7.260701.0
     */
    private static final String GROUP_CODE = "group-1";

    /**
     * 测试项目 ID。
     *
     * @since v7.260701.0
     */
    private static final String PROJECT_ID = "project-1";

    /**
     * 同一场景的多个分组复用相同设备类查询，同时保留各分组的实例化结果。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldReuseClassCodeQueryAndKeepGroupResults() {
        BizDtpRwdService rwdService = Mockito.mock(BizDtpRwdService.class);
        ShellConfigServiceImpl service = this.newService(rwdService);
        JSONObject system = this.newObject("system-1", "冷站一", "ACCC");
        Mockito.when(rwdService.queryObjectList(
                        Mockito.eq(GROUP_CODE), Mockito.eq(PROJECT_ID),
                        Mockito.eq(Collections.singletonList("ACCC")),
                        Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Collections.singletonList(system));
        ShellConfigGroup addSubtract = this.newGroup(
                ShellConfigGroupEnum.GROUP_ALG_ADD_SUBTRACT,
                ShellConfigItemEnum.ALG_COOLING_ADD_TIMING);
        ShellConfigGroup tempPressure = this.newGroup(
                ShellConfigGroupEnum.GROUP_ALG_TEMP_PRESS_DIFF,
                ShellConfigItemEnum.ALG_COOLING_SYSTEM_TEMP_DIFF_DEFAULT);

        List<ShellConfigGroup> results = service.getConfigValueByScene(
                GROUP_CODE, Arrays.asList(addSubtract, tempPressure), PROJECT_ID, null);

        Mockito.verify(rwdService, Mockito.times(1)).queryObjectList(
                GROUP_CODE, PROJECT_ID, Collections.singletonList("ACCC"),
                null, null, null, null);
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals("system-1", results.get(0).getItems().get(0).getObjectId());
        Assertions.assertEquals("冷站一", results.get(0).getItems().get(0).getObjectName());
        Assertions.assertEquals("system-1", results.get(1).getItems().get(0).getObjectId());
        Assertions.assertEquals("冷站一", results.get(1).getItems().get(0).getObjectName());
    }

    /**
     * 多个系统和重复配置关系只触发一次批量关系查询。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldBatchFrequencyRelationQueryForAllSystems() {
        BizDtpRwdService rwdService = Mockito.mock(BizDtpRwdService.class);
        ShellConfigServiceImpl service = this.newService(rwdService);
        List<JSONObject> systems = Arrays.asList(
                this.newObject("system-1", "冷站一", "ACCC"),
                this.newObject("system-2", "冷站二", "ACCC"));
        JSONObject pump = this.newObject("pump-1", "冷冻泵组一", "ACCPC");
        pump.put("netUseType", "1");
        Mockito.when(rwdService.queryObjectList(
                        Mockito.eq(GROUP_CODE), Mockito.eq(PROJECT_ID),
                        Mockito.eq(Collections.singletonList("ACCC")),
                        Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull()))
                .thenReturn(systems);
        Mockito.when(rwdService.queryObjectList(
                        Mockito.eq(GROUP_CODE), Mockito.eq(PROJECT_ID),
                        Mockito.eq(Collections.singletonList("ACCPC")),
                        Mockito.eq("CombineSubset"), Mockito.eq("Sy2Eg"),
                        Mockito.anyList(), Mockito.isNull()))
                .thenReturn(Collections.singletonList(pump));
        ShellConfigGroup frequencyGroup = this.newGroup(
                ShellConfigGroupEnum.GROUP_ALG_ACCC_FREQ,
                ShellConfigItemEnum.ALG_COOLING_PUMP_1_FREQ_RANGE,
                ShellConfigItemEnum.ALG_COOLING_PUMP_2_FREQ_RANGE);

        List<ShellConfigGroup> results = service.getConfigValueByScene(
                GROUP_CODE, Collections.singletonList(frequencyGroup), PROJECT_ID, null);

        Mockito.verify(rwdService, Mockito.times(1)).queryObjectList(
                Mockito.eq(GROUP_CODE), Mockito.eq(PROJECT_ID),
                Mockito.eq(Collections.singletonList("ACCPC")),
                Mockito.eq("CombineSubset"), Mockito.eq("Sy2Eg"),
                Mockito.argThat(ids -> ids.equals(Arrays.asList("system-1", "system-2"))),
                Mockito.isNull());
        Assertions.assertEquals(1, results.get(0).getItems().size());
        Assertions.assertEquals(
                ShellConfigItemEnum.ALG_COOLING_PUMP_1_FREQ_RANGE.getCode(),
                results.get(0).getItems().get(0).getCode());
        Assertions.assertEquals("pump-1", results.get(0).getItems().get(0).getObjectId());
    }

    /**
     * ACCI 控制对象一次读取并按系统复用计数。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldQueryAcciControlObjectsOnceForMultipleSystems() {
        BizDtpRwdService rwdService = Mockito.mock(BizDtpRwdService.class);
        ShellConfigServiceImpl service = this.newService(rwdService);
        List<JSONObject> systems = Arrays.asList(
                this.newObject("system-1", "冷热站一", "ACCI"),
                this.newObject("system-2", "冷热站二", "ACCI"));
        Mockito.when(rwdService.queryObjectList(
                        Mockito.eq(GROUP_CODE), Mockito.eq(PROJECT_ID),
                        Mockito.eq(Collections.singletonList("ACCI")),
                        Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull()))
                .thenReturn(systems);
        Mockito.when(rwdService.queryControlObjects(GROUP_CODE, PROJECT_ID, "ACAHC"))
                .thenReturn(Arrays.asList(
                        this.newControlObject("system-1"),
                        this.newControlObject("system-1"),
                        this.newControlObject("system-2")));
        ShellConfigItem item = new ShellConfigItem();
        item.setGroup(ShellConfigGroupEnum.GROUP_ALG_COLD_HOT.getCode());
        item.setCode("test-acci-item");
        ShellConfigGroup group = new ShellConfigGroup(
                ShellConfigGroupEnum.GROUP_ALG_COLD_HOT.getCode(),
                "tab",
                Collections.singletonList(item));

        List<ShellConfigGroup> results = service.getConfigValueByScene(
                GROUP_CODE, Collections.singletonList(group), PROJECT_ID, null);

        Mockito.verify(rwdService, Mockito.times(1))
                .queryControlObjects(GROUP_CODE, PROJECT_ID, "ACAHC");
        Assertions.assertEquals(
                Arrays.asList("system-1", "system-2"),
                results.get(0).getItems().stream()
                        .map(ShellConfigItem::getObjectId)
                        .collect(Collectors.toList()));
    }

    /**
     * 页面不包含天气数据源配置时不查询气象站。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldSkipWeatherStationQueryWhenWeatherItemIsAbsent() {
        BizDtpRwdService rwdService = Mockito.mock(BizDtpRwdService.class);
        ShellConfigServiceImpl service = this.newService(rwdService);
        ShellConfigGroup group = this.newGroup(
                ShellConfigGroupEnum.GROUP_ALG_PROJECT,
                ShellConfigItemEnum.ALG_PROJECT_POWER_ON);

        List<ShellConfigGroup> results = service.getConfigValueByScene(
                GROUP_CODE, Collections.singletonList(group), PROJECT_ID, null);

        Mockito.verifyNoInteractions(rwdService);
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(
                ShellConfigItemEnum.ALG_PROJECT_POWER_ON.getCode(),
                results.get(0).getItems().get(0).getCode());
    }

    /**
     * 创建仅注入 RWD 依赖的被测服务。
     *
     * @param rwdService RWD 服务
     * @return 被测服务
     * @since v7.260701.0
     */
    private @org.jspecify.annotations.NonNull ShellConfigServiceImpl newService(
            @org.jspecify.annotations.NonNull BizDtpRwdService rwdService) {
        ShellConfigServiceImpl service = new ShellConfigServiceImpl();
        ReflectionTestUtils.setField(service, "bizDtpRwdService", rwdService);
        return service;
    }

    /**
     * 按枚举定义创建壳配置分组。
     *
     * @param groupEnum 分组枚举
     * @param itemEnums 配置项枚举
     * @return 壳配置分组
     * @since v7.260701.0
     */
    private @org.jspecify.annotations.NonNull ShellConfigGroup newGroup(
            @org.jspecify.annotations.NonNull ShellConfigGroupEnum groupEnum,
            @org.jspecify.annotations.NonNull ShellConfigItemEnum... itemEnums) {
        List<ShellConfigItem> items = Arrays.stream(itemEnums)
                .map(this::newItem)
                .collect(Collectors.toList());
        return new ShellConfigGroup(groupEnum.getCode(), "tab", items);
    }

    /**
     * 构造场景组装所需的最小配置项，避免纯单测依赖国际化容器。
     *
     * @param itemEnum 配置项枚举
     * @return 壳配置项
     * @since v7.260701.0
     */
    private @org.jspecify.annotations.NonNull ShellConfigItem newItem(
            @org.jspecify.annotations.NonNull ShellConfigItemEnum itemEnum) {
        ShellConfigItem item = new ShellConfigItem();
        item.setGroup(itemEnum.getGroup());
        item.setCode(itemEnum.getCode());
        item.setDefaultValues(itemEnum.getDefaultValues());
        item.setHint(itemEnum.getHint());
        item.setMin(itemEnum.getMin());
        item.setMax(itemEnum.getMax());
        item.setValSource(itemEnum.getValSource());
        return item;
    }

    /**
     * 创建 RWD 对象。
     *
     * @param id 对象 ID
     * @param localName 对象名称
     * @param classCode 设备类编码
     * @return RWD 对象
     * @since v7.260701.0
     */
    private @org.jspecify.annotations.NonNull JSONObject newObject(
            @org.jspecify.annotations.NonNull String id,
            @org.jspecify.annotations.NonNull String localName,
            @org.jspecify.annotations.NonNull String classCode) {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("localName", localName);
        object.put("classCode", classCode);
        return object;
    }

    /**
     * 创建控制对象。
     *
     * @param systemId 所属系统 ID
     * @return 控制对象
     * @since v7.260701.0
     */
    private @org.jspecify.annotations.NonNull ControlObjectDto newControlObject(
            @org.jspecify.annotations.NonNull String systemId) {
        ControlObjectDto object = new ControlObjectDto();
        object.setSystemId(systemId);
        return object;
    }
}
