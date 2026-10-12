package com.persagy.apccontrol.basic.structure.cacherwd.service;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheInfoCodeTemplateService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 模板通用关系查询方向和空目标语义测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheTemplateRebuildServiceRelationTest {

    /**
     * 验证正向查询保留全部请求主体，并过滤请求范围外的关系目标。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldNormalizeRelationFromSubjects() throws Exception {
        DtpRwdClientService rwdClientService = Mockito.mock(DtpRwdClientService.class);
        CacheTemplateRebuildService service = this.newService(rwdClientService);
        DtpRwdObjectDTO hit = relationObject(
                "subject-1", "target-1", "outside-target");
        Mockito.when(rwdClientService.listObject(
                        Mockito.any(DtpRwdObjectQueryParam.class)))
                .thenReturn(Collections.singletonList(hit));

        List<DtpRwdObjectDTO> actual = invokeRelationMethod(
                service,
                "listRelationFromObjects",
                Arrays.asList("subject-1", "subject-2"),
                Collections.singletonList("target-1"),
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE);

        Map<String, Set<String>> expected = new LinkedHashMap<>();
        expected.put("subject-1", Collections.singleton("target-1"));
        expected.put("subject-2", Collections.emptySet());
        Assertions.assertEquals(expected, mapRelations(actual));

        ArgumentCaptor<DtpRwdObjectQueryParam> queryCaptor =
                ArgumentCaptor.forClass(DtpRwdObjectQueryParam.class);
        Mockito.verify(rwdClientService).listObject(queryCaptor.capture());
        DtpRwdObjectQueryParam query = queryCaptor.getValue();
        Assertions.assertEquals(
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE,
                query.getRelationFrom().getRelationType());
        Assertions.assertEquals(
                new LinkedHashSet<>(Arrays.asList("subject-1", "subject-2")),
                new LinkedHashSet<>(query.getId()));
    }

    /**
     * 验证反向查询结果转换为原关系方向，并为无命中来源保留空目标。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldInvertRelationToTargetsBySource() throws Exception {
        DtpRwdClientService rwdClientService = Mockito.mock(DtpRwdClientService.class);
        CacheTemplateRebuildService service = this.newService(rwdClientService);
        Mockito.when(rwdClientService.listObject(
                        Mockito.any(DtpRwdObjectQueryParam.class)))
                .thenReturn(Arrays.asList(
                        relationObject("target-1", "source-1", "outside-source"),
                        relationObject("target-2", "source-1", "source-2"),
                        relationObject("outside-target", "source-2")));

        List<DtpRwdObjectDTO> actual = invokeRelationMethod(
                service,
                "listInvertedRelationToObjects",
                Arrays.asList("target-1", "target-2"),
                Arrays.asList("source-1", "source-2", "source-empty"),
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE);

        Map<String, Set<String>> expected = new LinkedHashMap<>();
        expected.put("source-1", new LinkedHashSet<>(
                Arrays.asList("target-1", "target-2")));
        expected.put("source-2", Collections.singleton("target-2"));
        expected.put("source-empty", Collections.emptySet());
        Assertions.assertEquals(expected, mapRelations(actual));

        ArgumentCaptor<DtpRwdObjectQueryParam> queryCaptor =
                ArgumentCaptor.forClass(DtpRwdObjectQueryParam.class);
        Mockito.verify(rwdClientService).listObject(queryCaptor.capture());
        Assertions.assertEquals(
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE,
                queryCaptor.getValue().getRelationTo().getRelationType());
    }

    /**
     * 验证模板 JSON 只提供图谱和关系编码时仍能映射到枚举。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldMapGraphAndRelationCodesToEnum() throws Exception {
        CacheTemplateRebuildService service = this.newService(
                Mockito.mock(DtpRwdClientService.class));
        DtpRwdRelationTypeEnum expected = DtpRwdRelationTypeEnum.EQUIP_IN_GROUP;
        DtpRwdObjectRelationQueryParam relation =
                new DtpRwdObjectRelationQueryParam(
                        expected.value().getLeft(), expected.value().getRight());
        Method method = CacheTemplateRebuildService.class.getDeclaredMethod(
                "mapRelationType", DtpRwdObjectRelationQueryParam.class);
        method.setAccessible(true);

        Object actual = method.invoke(service, relation);

        Assertions.assertEquals(expected, actual);
    }

    /**
     * 验证同一关系类型的正反向结果先按主体合并，再作为一次完整替换提交。
     *
     * @since v7.260701.0
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldMergeBothDirectionsOfSameRelationType() throws Exception {
        DtpRwdClientService rwdClientService = Mockito.mock(DtpRwdClientService.class);
        CacheTemplateRebuildService service = this.newService(rwdClientService);
        DtpRwdRelationTypeEnum relationType =
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE;
        Mockito.when(rwdClientService.listObject(
                        Mockito.any(DtpRwdObjectQueryParam.class)))
                .thenAnswer(invocation -> {
                    DtpRwdObjectQueryParam query = invocation.getArgument(0);
                    return query.getRelationFrom() == null
                            ? Collections.singletonList(
                                    relationObject("target-2", "shared-subject"))
                            : Collections.singletonList(
                                    relationObject("shared-subject", "forward-target"));
                });
        DtpRwdObjectQueryParam relationQuery = new DtpRwdObjectQueryParam();
        relationQuery.setRelationFrom(new DtpRwdObjectRelationQueryParam(relationType)
                .setObjTo("forward-target"));
        relationQuery.setRelationTo(new DtpRwdObjectRelationQueryParam(relationType)
                .setObjFrom("shared-subject"));
        List<DtpRwdObjectDTO> objects = Arrays.asList(
                relationObject("shared-subject"), relationObject("target-2"));
        Method method = CacheTemplateRebuildService.class.getDeclaredMethod(
                "prepareObjectRelations", Collection.class,
                DtpRwdObjectQueryParam.class);
        method.setAccessible(true);

        List<?> preparedRelations = (List<?>) method.invoke(
                service, objects, relationQuery);

        Assertions.assertEquals(1, preparedRelations.size());
        Object preparedRelation = preparedRelations.get(0);
        Field relationTypeField = preparedRelation.getClass()
                .getDeclaredField("relationType");
        relationTypeField.setAccessible(true);
        Assertions.assertEquals(relationType, relationTypeField.get(preparedRelation));
        Field relationObjectsField = preparedRelation.getClass()
                .getDeclaredField("relationObjects");
        relationObjectsField.setAccessible(true);
        Map<String, Set<String>> actual = mapRelations(
                (Collection<? extends DtpRwdObjectDTO>)
                        relationObjectsField.get(preparedRelation));
        Map<String, Set<String>> expected = new LinkedHashMap<>();
        expected.put("shared-subject", new LinkedHashSet<>(
                Arrays.asList("forward-target", "target-2")));
        expected.put("target-2", Collections.emptySet());
        Assertions.assertEquals(expected, actual);
    }

    private CacheTemplateRebuildService newService(
            DtpRwdClientService rwdClientService) {
        return new CacheTemplateRebuildService(
                Mockito.mock(CacheObjectModelWriteService.class),
                rwdClientService,
                Mockito.mock(CacheRwdDomainService.class),
                Mockito.mock(CacheObjectModelStatusService.class),
                Mockito.mock(CacheInfoCodeTemplateService.class),
                Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    private static List<DtpRwdObjectDTO> invokeRelationMethod(
            CacheTemplateRebuildService service,
            String methodName,
            Collection<String> firstIds,
            Collection<String> secondIds,
            DtpRwdRelationTypeEnum relationType) throws Exception {
        Method method = CacheTemplateRebuildService.class.getDeclaredMethod(
                methodName, Collection.class, Collection.class,
                DtpRwdRelationTypeEnum.class);
        method.setAccessible(true);
        return (List<DtpRwdObjectDTO>) method.invoke(
                service, firstIds, secondIds, relationType);
    }

    private static DtpRwdObjectDTO relationObject(
            String objectId,
            String... relationObjectIds) {
        DtpRwdObjectDTO result = new DtpRwdObjectDTO();
        result.setId(objectId);
        result.setRelObjIds(new LinkedHashSet<>(Arrays.asList(relationObjectIds)));
        return result;
    }

    private static Map<String, Set<String>> mapRelations(
            Collection<? extends DtpRwdObjectDTO> relations) {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (DtpRwdObjectDTO relation : relations) {
            result.put(relation.getId(), relation.getRelObjIds());
        }
        return result;
    }
}
