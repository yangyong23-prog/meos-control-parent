package com.persagy.apccontrol.basic.structure.demand.listener;

import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigResultRefreshRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigResultRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * 需求配置结果刷新事件监听器测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
class DemandConfigResultRefreshEventListenerTest {

    /**
     * 项目配置的最终事件必须刷新项目及其全部下级实例。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldRefreshProjectAndChildrenForProjectConfigChange() {
        DemandConfigResultRefreshRepository refreshRepository =
                Mockito.mock(DemandConfigResultRefreshRepository.class);
        DemandConfigResultRepository resultRepository =
                Mockito.mock(DemandConfigResultRepository.class);
        DemandConfigResultRefreshEventListener listener =
                new DemandConfigResultRefreshEventListener(refreshRepository, resultRepository);
        Mockito.when(refreshRepository.supportsTemplate(101L)).thenReturn(true);

        DemandConfig projectConfig = new DemandConfig();
        projectConfig.setDemandInstanceType(DemandInstanceTypeEnum.PROJECT);
        projectConfig.setDemandInstanceId("project-1");
        DemandConfigChangeEvent.Detail detail = new DemandConfigChangeEvent.Detail(
                DataTypeEnum.EDIT,
                Collections.singletonList(new DemandConfigChangeEvent.Config(null, projectConfig)));
        DemandConfigChangeEvent.Message message = new DemandConfigChangeEvent.Message();
        message.setTemplateId(101L);
        message.setDetails(Collections.singletonList(detail));

        listener.onDemandConfigChangeEvent(new DemandConfigChangeEvent(message));

        Mockito.verify(refreshRepository).refreshByInstanceAndChildren(
                101L,
                null,
                DemandInstanceTypeEnum.PROJECT,
                Collections.singleton("project-1"));
        Mockito.verifyNoInteractions(resultRepository);
    }
}
