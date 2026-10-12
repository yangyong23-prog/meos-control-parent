package com.persagy.apccontrol.basic.structure.infocodeconfig.repository.impl;

import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeImportContext;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeSheetImportContext;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeConfigResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeGroupResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeTemplateTypeResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeRemoveDaoParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * 信息点配置导入旧模板范围捕获测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class InfoCodeRepositoryImplTest {

    /**
     * 导入时应在公共库删除旧关联配置，并返回旧模板 ID 供上层跨项目清理缓存。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReturnOldTemplateIdsAndDeletePublicConfigsBeforeImportReplacement() {
        InfoCodeConverter converter = Mockito.mock(InfoCodeConverter.class);
        InfoCodeGroupResource groupResource = Mockito.mock(InfoCodeGroupResource.class);
        InfoCodeConfigResource configResource = Mockito.mock(InfoCodeConfigResource.class);
        InfoCodeTemplateTypeResource templateTypeResource =
                Mockito.mock(InfoCodeTemplateTypeResource.class);
        InfoCodeRepositoryImpl repository = new InfoCodeRepositoryImpl(
                converter, groupResource, configResource, templateTypeResource);
        InfoCodeImportContext context = Mockito.mock(InfoCodeImportContext.class);
        InfoCodeSheetImportContext sheetContext = Mockito.mock(InfoCodeSheetImportContext.class);
        Mockito.when(context.getRunningMonitorType())
                .thenReturn(RunningMonitorTypeEnum.AIR_TERMINAL);
        Mockito.when(context.getRunningMonitorSecondType())
                .thenReturn(RunningMonitorSecondTypeEnum.AIR_TERMINAL);
        Mockito.when(context.getTemplateType()).thenReturn(InfoCodeTemplateTypeEnum.GRID);
        Mockito.when(context.getSheetContexts())
                .thenReturn(Collections.singletonList(sheetContext));
        Mockito.when(sheetContext.getClassCode()).thenReturn("ACATFC");
        Mockito.when(sheetContext.getConfigContexts()).thenReturn(Collections.emptyList());
        InfoCodeTemplateType oldTemplate = new InfoCodeTemplateType();
        oldTemplate.setId(101L);
        InfoCodeTemplateType newTemplate = new InfoCodeTemplateType();
        newTemplate.setId(202L);
        Mockito.when(templateTypeResource.list(
                        Mockito.any(InfoCodeTemplateTypeQueryDaoParam.class)))
                .thenReturn(Collections.singletonList(oldTemplate));
        Mockito.when(converter.toEntity(
                        Mockito.same(sheetContext),
                        Mockito.eq(RunningMonitorTypeEnum.AIR_TERMINAL),
                        Mockito.eq(RunningMonitorSecondTypeEnum.AIR_TERMINAL),
                        Mockito.eq(InfoCodeTemplateTypeEnum.GRID)))
                .thenReturn(newTemplate);
        Mockito.when(templateTypeResource.add(Mockito.same(newTemplate), Mockito.eq(false)))
                .thenReturn(newTemplate);
        Mockito.when(configResource.addForce(Mockito.anyList()))
                .thenReturn(Collections.emptyList());

        Assertions.assertEquals(Collections.singleton(101L), repository.addSheetConfig(context));

        ArgumentCaptor<InfoCodeTemplateTypeQueryDaoParam> queryCaptor =
                ArgumentCaptor.forClass(InfoCodeTemplateTypeQueryDaoParam.class);
        Mockito.verify(templateTypeResource).list(queryCaptor.capture());
        InfoCodeTemplateTypeQueryDaoParam queryParam = queryCaptor.getValue();
        Assertions.assertEquals(RunningMonitorTypeEnum.AIR_TERMINAL,
                queryParam.getRunningMonitorType());
        Assertions.assertEquals(RunningMonitorSecondTypeEnum.AIR_TERMINAL,
                queryParam.getRunningMonitorSecondType());
        Assertions.assertEquals(InfoCodeTemplateTypeEnum.GRID, queryParam.getTemplateType());
        Assertions.assertEquals(Collections.singleton("ACATFC"), queryParam.getClassCodes());
        InOrder deletionOrder = Mockito.inOrder(configResource, templateTypeResource);
        deletionOrder.verify(configResource).del(Collections.singleton(101L));
        deletionOrder.verify(templateTypeResource).del(
                Mockito.any(InfoCodeTemplateTypeRemoveDaoParam.class), Mockito.eq(false));
    }
}
