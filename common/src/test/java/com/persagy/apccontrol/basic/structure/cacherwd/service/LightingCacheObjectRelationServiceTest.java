package com.persagy.apccontrol.basic.structure.cacherwd.service;

import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.NonNull;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Set;

/**
 * 照明关系写入范围回归测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class LightingCacheObjectRelationServiceTest {

    /**
     * 验证空结果只按当前 Key 的旧回路分别删除正向和反向关系。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReplaceOnlyCurrentKeyRelations() {
        ContextUtil.setGroupCode("group-1");
        ContextUtil.setProjectId("project-1");
        DtpRwdClientService rwdClientService = Mockito.mock(DtpRwdClientService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        CacheObjectRelRepository relationRepository = Mockito.mock(CacheObjectRelRepository.class);
        CacheObjectRecordConverter objectConverter = Mockito.mock(CacheObjectRecordConverter.class);
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        LightingCacheObjectRelationService service = new LightingCacheObjectRelationService(
                rwdClientService, objectRepository, modelViewRepository,
                relationRepository, objectConverter, modelViewConverter);

        Set<String> previousCircuitIds = Collections.singleton("removed-circuit");
        Mockito.when(objectRepository.listObjectIdsByCacheKey("empty-key"))
                .thenReturn(Collections.singletonList("removed-circuit"));

        CacheWriteAction action = service.prepareSaveAction(
                "empty-key", Collections.emptyList(), null);
        action.execute();

        LightingCacheObjectRelationServiceTest.verifyForwardRemove(
                relationRepository, previousCircuitIds,
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE);
        LightingCacheObjectRelationServiceTest.verifyForwardRemove(
                relationRepository, previousCircuitIds,
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_FLOOR);
        LightingCacheObjectRelationServiceTest.verifyReverseRemove(
                relationRepository, previousCircuitIds,
                DtpRwdRelationTypeEnum.EQUIPMENT_CONTROLLER);
        LightingCacheObjectRelationServiceTest.verifyReverseRemove(
                relationRepository, previousCircuitIds,
                DtpRwdRelationTypeEnum.WE_NETWORK);
        Mockito.verify(relationRepository).addRelations(
                Mockito.eq(Collections.<CacheObjectRel>emptyList()));
        Mockito.verifyNoInteractions(rwdClientService);
    }

    /**
     * 核对指定关系类型的正向删除调用。
     *
     * @param repository 关系仓储
     * @param circuitIds 回路 ID
     * @param relation 关系类型
     * @since v7.260701.0
     */
    private static void verifyForwardRemove(
            @NonNull CacheObjectRelRepository repository,
            @NonNull Set<String> circuitIds,
            @NonNull DtpRwdRelationTypeEnum relation) {
        Mockito.verify(repository).removeForwardRelations(
                circuitIds, relation.value().getLeft(), relation.value().getRight());
    }

    /**
     * 核对指定关系类型的反向删除调用。
     *
     * @param repository 关系仓储
     * @param circuitIds 回路 ID
     * @param relation 关系类型
     * @since v7.260701.0
     */
    private static void verifyReverseRemove(
            @NonNull CacheObjectRelRepository repository,
            @NonNull Set<String> circuitIds,
            @NonNull DtpRwdRelationTypeEnum relation) {
        Mockito.verify(repository).removeReverseRelations(
                circuitIds, relation.value().getLeft(), relation.value().getRight());
    }
}
