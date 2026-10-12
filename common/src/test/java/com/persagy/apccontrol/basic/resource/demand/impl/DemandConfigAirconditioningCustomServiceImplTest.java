package com.persagy.apccontrol.basic.resource.demand.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.controller.shellconfig.param.ShellConfigVO;
import com.persagy.apccontrol.basic.mapper.demand.DemandConfigAirconditioningCustomMapper;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigSceneEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.demand.param.airconditioning.DemandConfigAirconditioningCustomQueryDaoParam;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkConditionValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkInstanceValueObject;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

/**
 * 空调自定义配置保存测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
class DemandConfigAirconditioningCustomServiceImplTest {

    /**
     * 清理测试线程上下文。
     *
     * @since v7.260701.0
     */
    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    /**
     * 保存接口在返回前同步完成壳配置回写。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldBackwriteShellConfigSynchronously() {
        DemandConfigAirconditioningCustomServiceImpl service =
                Mockito.spy(new DemandConfigAirconditioningCustomServiceImpl());
        Mockito.doReturn(true).when(service).addOrEditCustomConfig(Mockito.eq(101L), Mockito.any());
        Mockito.doReturn(true).when(service)
                .wirteShellConfigParam("group-1", "project-1", "101");
        ContextUtil.setGroupCode("group-1");
        ContextUtil.setTenant("group-1");
        ContextUtil.setProjectId("project-1");

        service.saveOrUpdateConfigData(
                "project-1", 101L, null, new DemandConfigAirconditioningWorkConditionValueObject());

        Mockito.verify(service, Mockito.times(1))
                .wirteShellConfigParam("group-1", "project-1", "101");
    }

    /**
     * 壳配置回写直接使用当前 RWD 重建工况中的设备类编码。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldClassifyRwdBuiltWorkConditionWithoutAnotherObjectQuery() {
        DemandConfigAirconditioningCustomServiceImpl service =
                Mockito.spy(new DemandConfigAirconditioningCustomServiceImpl());
        DtpRwdClientService rwdClientService = Mockito.mock(DtpRwdClientService.class);
        AbstractCommonServer commonServer = Mockito.mock(AbstractCommonServer.class);
        ShellConfigService shellConfigService = Mockito.mock(ShellConfigService.class);
        ReflectionTestUtils.setField(service, "dtpRwdClientService", rwdClientService);
        ReflectionTestUtils.setField(service, "abstractCommonServer", commonServer);
        ReflectionTestUtils.setField(service, "shellConfigService", shellConfigService);

        DemandConfigAirconditioningWorkConditionValueObject workCondition =
                new DemandConfigAirconditioningWorkConditionValueObject();
        workCondition.setCoolingSeason(Collections.singletonList(
                this.newWorkInstance("cool-system", DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue())));
        workCondition.setHeatingSeason(Collections.singletonList(
                this.newWorkInstance("heat-system", DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue())));
        DemandConfigAirconditioningCustomDTO configData = new DemandConfigAirconditioningCustomDTO();
        configData.setWorkCondition(workCondition);
        Mockito.doReturn(configData).when(service).getConfigData("project-1", "101", null);
        ShellConfigVO shellConfig = new ShellConfigVO();
        shellConfig.setGroups(Collections.emptyList());
        Mockito.when(commonServer.queryShellConfigByScene(
                        "group-1", "project-1", ShellConfigSceneEnum.SCENE_ALG.getCode(), null))
                .thenReturn(Collections.singletonList(shellConfig));

        service.wirteShellConfigParam("group-1", "project-1", "101");

        Mockito.verify(rwdClientService, Mockito.never())
                .listObject(Mockito.any(DtpRwdObjectQueryParam.class));
    }

    /**
     * 定制配置只需读取一行，避免列表查询和多余映射。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldLoadCustomConfigWithSingleRowQuery() {
        DemandConfigAirconditioningCustomServiceImpl service =
                new DemandConfigAirconditioningCustomServiceImpl();
        DemandConfigAirconditioningCustomMapper mapper =
                Mockito.mock(DemandConfigAirconditioningCustomMapper.class);
        AbstractCommonServer commonServer = Mockito.mock(AbstractCommonServer.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        ReflectionTestUtils.setField(service, "abstractCommonServer", commonServer);
        Mockito.when(commonServer.queryObjectsExistRuleMap(
                        Mockito.anyString(), Mockito.anyString(), Mockito.anySet()))
                .thenReturn(Collections.emptyMap());
        ContextUtil.setGroupCode("group-1");
        ContextUtil.setTenant("group-1");
        ContextUtil.setProjectId("project-1");

        service.getConfigData("project-1", "101", null);

        Mockito.verify(mapper).get(Mockito.any(DemandConfigAirconditioningCustomQueryDaoParam.class));
        Mockito.verify(mapper, Mockito.never())
                .list(Mockito.any(DemandConfigAirconditioningCustomQueryDaoParam.class));
    }

    /**
     * 创建带有 RWD 设备类编码的工况系统。
     *
     * @param id 系统 ID
     * @param classCode 设备类编码
     * @return 工况系统
     * @since v7.260701.0
     */
    private DemandConfigAirconditioningWorkInstanceValueObject newWorkInstance(
            String id,
            String classCode) {
        DemandConfigAirconditioningWorkInstanceConfigValueObject systemConfig =
                new DemandConfigAirconditioningWorkInstanceConfigValueObject();
        systemConfig.setId(id);
        systemConfig.setClassCode(classCode);
        DemandConfigAirconditioningWorkInstanceValueObject instance =
                new DemandConfigAirconditioningWorkInstanceValueObject();
        instance.setSystemConfig(systemConfig);
        return instance;
    }
}
