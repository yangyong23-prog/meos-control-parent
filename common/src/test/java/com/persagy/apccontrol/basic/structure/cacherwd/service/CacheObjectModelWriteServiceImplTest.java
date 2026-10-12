package com.persagy.apccontrol.basic.structure.cacherwd.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelReplaceScope;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleContext;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 缓存对象模型纯本地写入边界测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheObjectModelWriteServiceImplTest {

    /**
     * 项目 ObjectMapper 静态字段。
     */
    private Field objectMapperField;

    /**
     * 测试前的项目 ObjectMapper。
     */
    private Object originalObjectMapper;

    @BeforeEach
    public void setUpObjectMapper() throws Exception {
        LocaleContext.setLocaleContext(Locale.ENGLISH);
        this.objectMapperField = ObjectMapperUtils.class.getDeclaredField("objectMapper");
        this.objectMapperField.setAccessible(true);
        this.originalObjectMapper = this.objectMapperField.get(null);
        this.objectMapperField.set(null, new ObjectMapper());
    }

    @AfterEach
    public void restoreObjectMapper() throws Exception {
        this.objectMapperField.set(null, this.originalObjectMapper);
        LocaleContext.remove();
        ContextUtil.remove();
    }

    /**
     * 创建同步执行锁内动作的本地锁服务，保持单元测试只验证写服务行为。
     *
     * @return 同步执行动作的锁服务 mock
     * @since v7.260802.0
     */
    private RedissonDistributedLockerService localLocker() {
        RedissonDistributedLockerService locker =
                Mockito.mock(RedissonDistributedLockerService.class);
        Mockito.doAnswer(invocation -> {
            Supplier<?> action = invocation.getArgument(2);
            return action.get();
        }).when(locker).executeWithLock(
                Mockito.anyString(), Mockito.anyLong(), Mockito.any(Supplier.class),
                Mockito.anyLong(), Mockito.anyLong());
        return locker;
    }

    /**
     * 验证源快照在事务前投影，有序扩展动作在对象事务中原样提交。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldCommitExplicitCallerDataInOrder() {
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        CacheObjectRecordConverter objectConverter =
                Mockito.mock(CacheObjectRecordConverter.class);
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        PlatformTransactionManager transactionManager =
                Mockito.mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = Mockito.mock(TransactionStatus.class);
        Mockito.when(transactionManager.getTransaction(
                        Mockito.any(TransactionDefinition.class)))
                .thenReturn(transactionStatus);
        RedissonDistributedLockerService locker = this.localLocker();
        CacheObjectModelWriteServiceImpl service = new CacheObjectModelWriteServiceImpl(
                objectRepository, modelViewRepository,
                relationRepository, objectConverter, modelViewConverter,
                transactionManager, locker);

        DtpRwdObjectDTO sourceObject = new DtpRwdObjectDTO();
        sourceObject.setId("source-id");
        CacheObject object = new CacheObject();
        object.setId("persisted-row-id");
        CacheObjectModelView modelView = new CacheObjectModelView();
        modelView.setId("persisted-view-id");
        List<CacheObject> objects = Collections.singletonList(object);
        List<CacheObjectModelView> modelViews = Collections.singletonList(modelView);
        Mockito.when(objectConverter.toCacheObject(Mockito.anyMap())).thenReturn(object);
        Mockito.when(modelViewConverter.toModelView(Mockito.anyMap())).thenReturn(modelView);
        CacheWriteAction firstAction = Mockito.mock(CacheWriteAction.class);
        CacheWriteAction secondAction = Mockito.mock(CacheWriteAction.class);

        boolean result = service.replaceModelCacheKey(
                "model-key", Collections.singletonList(sourceObject),
                Arrays.asList(firstAction, secondAction));

        Assertions.assertTrue(result);
        InOrder inOrder = Mockito.inOrder(
                objectConverter, modelViewConverter, locker, transactionManager,
                objectRepository, modelViewRepository,
                firstAction, secondAction);
        inOrder.verify(objectConverter).toCacheObject(Mockito.anyMap());
        inOrder.verify(modelViewConverter).toModelView(Mockito.anyMap());
        inOrder.verify(locker).executeWithLock(
                Mockito.anyString(), Mockito.eq(120L), Mockito.any(Supplier.class),
                Mockito.eq(5000L), Mockito.eq(10000L));
        inOrder.verify(transactionManager).getTransaction(
                Mockito.any(TransactionDefinition.class));
        inOrder.verify(objectRepository).upsertObjects(objects);
        inOrder.verify(modelViewRepository).upsertModelViews(modelViews);
        inOrder.verify(objectRepository).replaceObjectIds(
                "model-key", Collections.singletonList("persisted-row-id"));
        inOrder.verify(firstAction).execute();
        inOrder.verify(secondAction).execute();
        inOrder.verify(transactionManager).commit(transactionStatus);
        Mockito.verifyNoInteractions(relationRepository);
    }

    /**
     * 所有会写对象主表或模型视图表的入口都必须使用同一项目级保存锁。
     *
     * @since v7.260802.0
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldLockAllObjectTableWriteEntries() {
        ContextUtil.setProjectId("project-lock-scope");
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        PlatformTransactionManager transactionManager =
                Mockito.mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = Mockito.mock(TransactionStatus.class);
        Mockito.when(transactionManager.getTransaction(
                        Mockito.any(TransactionDefinition.class)))
                .thenReturn(transactionStatus);
        RedissonDistributedLockerService locker = this.localLocker();
        CacheObjectModelWriteServiceImpl service = new CacheObjectModelWriteServiceImpl(
                objectRepository,
                modelViewRepository,
                Mockito.mock(CacheObjectRelRepository.class),
                Mockito.mock(CacheObjectRecordConverter.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class),
                transactionManager,
                locker);

        Assertions.assertTrue(service.replaceModelCacheKey(
                "single-key", Collections.emptyList()));
        Assertions.assertTrue(service.replaceModelCacheKeys(
                Collections.emptyMap(), Collections.emptyList()));
        Assertions.assertTrue(service.replaceModelCacheKeysAndRelations(
                Collections.emptyMap(), Collections.emptyList(),
                Collections.emptyMap(), () -> true));
        Assertions.assertTrue(service.replaceObjectClassCodes(
                Collections.singletonList("BUILDING"), Collections.emptyList()));

        ArgumentCaptor<String> lockKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> waitSecondsCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> firstRetryDelayCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> secondRetryDelayCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(locker, Mockito.times(4)).executeWithLock(
                lockKeyCaptor.capture(), waitSecondsCaptor.capture(),
                Mockito.any(Supplier.class),
                firstRetryDelayCaptor.capture(), secondRetryDelayCaptor.capture());
        String expectedLockKey = CacheLockKey.CacheObjectModelWrite.cache().getKey();
        Assertions.assertEquals(
                Arrays.asList(expectedLockKey, expectedLockKey, expectedLockKey, expectedLockKey),
                lockKeyCaptor.getAllValues());
        Assertions.assertEquals(
                Arrays.asList(120L, 120L, 120L, 120L),
                waitSecondsCaptor.getAllValues());
        Assertions.assertEquals(
                Arrays.asList(5000L, 5000L, 5000L, 5000L),
                firstRetryDelayCaptor.getAllValues());
        Assertions.assertEquals(
                Arrays.asList(10000L, 10000L, 10000L, 10000L),
                secondRetryDelayCaptor.getAllValues());
        Mockito.verify(transactionManager, Mockito.times(4)).getTransaction(
                Mockito.any(TransactionDefinition.class));
        Mockito.verify(transactionManager, Mockito.times(4)).commit(transactionStatus);
    }

    /**
     * 验证关系入口把 DTO 转换为指定枚举范围，并保留空目标清理语义。
     *
     * @since v7.260701.0
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldConvertRelationDtosInsideLocalTransaction() {
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        PlatformTransactionManager transactionManager =
                Mockito.mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = Mockito.mock(TransactionStatus.class);
        Mockito.when(transactionManager.getTransaction(
                        Mockito.any(TransactionDefinition.class)))
                .thenReturn(transactionStatus);
        CacheObjectModelWriteServiceImpl service = new CacheObjectModelWriteServiceImpl(
                Mockito.mock(CacheObjectRepository.class),
                Mockito.mock(CacheObjectModelViewRepository.class),
                relationRepository,
                Mockito.mock(CacheObjectRecordConverter.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class),
                transactionManager, this.localLocker());
        DtpRwdObjectDTO emptyRelation = new DtpRwdObjectDTO();
        emptyRelation.setId("subject-empty");
        DtpRwdObjectDTO populatedRelation = new DtpRwdObjectDTO();
        populatedRelation.setId("subject-populated");
        populatedRelation.setRelObjIds(new LinkedHashSet<>(
                Collections.singletonList("target-1")));

        service.replaceObjectRelations(
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE,
                Arrays.asList(emptyRelation, populatedRelation));

        ArgumentCaptor<Collection<CacheObjectRelReplaceScope>> scopeCaptor =
                ArgumentCaptor.forClass((Class) Collection.class);
        InOrder inOrder = Mockito.inOrder(transactionManager, relationRepository);
        inOrder.verify(transactionManager).getTransaction(
                Mockito.any(TransactionDefinition.class));
        inOrder.verify(relationRepository).replaceRelations(scopeCaptor.capture());
        inOrder.verify(transactionManager).commit(transactionStatus);
        List<CacheObjectRelReplaceScope> scopes =
                new ArrayList<>(scopeCaptor.getValue());
        Assertions.assertEquals(2, scopes.size());
        Assertions.assertEquals("subject-empty", scopes.get(0).getObjectId());
        Assertions.assertEquals(
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE.value().getLeft(),
                scopes.get(0).getGraphCode());
        Assertions.assertEquals(
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE.value().getRight(),
                scopes.get(0).getRelCode());
        Assertions.assertTrue(scopes.get(0).getRelObjectIds().isEmpty());
        Assertions.assertEquals(
                Collections.singletonList("target-1"),
                scopes.get(1).getRelObjectIds());
    }

    /**
     * 验证非法关系主体在开启事务和删除旧范围前被业务校验拒绝。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldRejectInvalidRelationScopesBeforeTransaction() {
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        PlatformTransactionManager transactionManager =
                Mockito.mock(PlatformTransactionManager.class);
        CacheObjectModelWriteServiceImpl service = new CacheObjectModelWriteServiceImpl(
                Mockito.mock(CacheObjectRepository.class),
                Mockito.mock(CacheObjectModelViewRepository.class),
                relationRepository,
                Mockito.mock(CacheObjectRecordConverter.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class),
                transactionManager, this.localLocker());
        DtpRwdObjectDTO invalidRelation = new DtpRwdObjectDTO();
        invalidRelation.setId(" ");
        DtpRwdObjectDTO invalidTarget = new DtpRwdObjectDTO();
        invalidTarget.setId("subject-with-invalid-target");
        invalidTarget.setRelObjIds(new LinkedHashSet<>(Collections.singletonList(" ")));
        DtpRwdObjectDTO duplicateSubject = new DtpRwdObjectDTO();
        duplicateSubject.setId("duplicate-subject");

        Assertions.assertThrows(
                BusinessException.class,
                () -> service.replaceObjectRelations(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE,
                        Collections.singletonList(invalidRelation)));
        Assertions.assertThrows(
                BusinessException.class,
                () -> service.replaceObjectRelations(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE,
                        Collections.singletonList(invalidTarget)));
        Assertions.assertThrows(
                BusinessException.class,
                () -> service.replaceObjectRelations(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE,
                        Arrays.asList(duplicateSubject, duplicateSubject)));

        Mockito.verifyNoInteractions(transactionManager, relationRepository);
    }

    /**
     * 验证本地提交回调返回 false 时标记整个 Key 替换事务回滚。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldRollbackWhenAfterReplaceActionRejectsCommit() {
        ContextUtil.setProjectId("project-1");
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        PlatformTransactionManager transactionManager =
                Mockito.mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = Mockito.mock(TransactionStatus.class);
        Mockito.when(transactionManager.getTransaction(
                        Mockito.any(TransactionDefinition.class)))
                .thenReturn(transactionStatus);
        CacheObjectModelWriteServiceImpl service = new CacheObjectModelWriteServiceImpl(
                objectRepository, modelViewRepository,
                relationRepository,
                Mockito.mock(CacheObjectRecordConverter.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class),
                transactionManager, this.localLocker());

        boolean result = service.replaceModelCacheKey(
                "model-key", Collections.emptyList(),
                Collections.emptyList(), () -> false);

        Assertions.assertFalse(result);
        Mockito.verify(transactionStatus).setRollbackOnly();
        Mockito.verify(transactionManager).commit(transactionStatus);
    }

    /**
     * 验证任一本地动作失败时异常上抛并回滚已经执行的 Key 替换 SQL。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldRollbackAndPropagateLocalWriteFailure() {
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        PlatformTransactionManager transactionManager =
                Mockito.mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = Mockito.mock(TransactionStatus.class);
        Mockito.when(transactionManager.getTransaction(
                        Mockito.any(TransactionDefinition.class)))
                .thenReturn(transactionStatus);
        CacheObjectModelWriteServiceImpl service = new CacheObjectModelWriteServiceImpl(
                objectRepository, modelViewRepository,
                relationRepository,
                Mockito.mock(CacheObjectRecordConverter.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class),
                transactionManager, this.localLocker());
        IllegalStateException failure = new IllegalStateException("local write failed");
        CacheWriteAction failedAction = () -> {
            throw failure;
        };

        IllegalStateException actual = Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.replaceModelCacheKey(
                        "model-key", Collections.emptyList(),
                        Collections.singletonList(failedAction)));

        Assertions.assertSame(failure, actual);
        Mockito.verify(objectRepository).replaceObjectIds(
                "model-key", Collections.emptyList());
        Mockito.verify(transactionManager).rollback(transactionStatus);
        Mockito.verify(transactionManager, Mockito.never()).commit(transactionStatus);
    }

    /**
     * 验证多 Key 的所有对象和模型视图均在事务前投影，并按 Key、关系动作顺序提交。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldPrepareAllKeysBeforeTransactionAndCommitInMapOrder() {
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        CacheObjectRecordConverter objectConverter =
                Mockito.mock(CacheObjectRecordConverter.class);
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        PlatformTransactionManager transactionManager =
                Mockito.mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = Mockito.mock(TransactionStatus.class);
        Mockito.when(transactionManager.getTransaction(
                        Mockito.any(TransactionDefinition.class)))
                .thenReturn(transactionStatus);
        CacheObjectModelWriteServiceImpl service = new CacheObjectModelWriteServiceImpl(
                objectRepository, modelViewRepository,
                relationRepository, objectConverter, modelViewConverter,
                transactionManager, this.localLocker());

        DtpRwdObjectDTO firstSource = new DtpRwdObjectDTO();
        firstSource.setId("source-1");
        DtpRwdObjectDTO secondSource = new DtpRwdObjectDTO();
        secondSource.setId("source-2");
        CacheObject firstObject = new CacheObject();
        firstObject.setId("object-1");
        CacheObject secondObject = new CacheObject();
        secondObject.setId("object-2");
        CacheObjectModelView firstView = new CacheObjectModelView();
        firstView.setId("view-1");
        CacheObjectModelView secondView = new CacheObjectModelView();
        secondView.setId("view-2");
        Mockito.when(objectConverter.toCacheObject(Mockito.anyMap()))
                .thenReturn(firstObject, secondObject);
        Mockito.when(modelViewConverter.toModelView(Mockito.anyMap()))
                .thenReturn(firstView, secondView);
        CacheWriteAction relationAction = Mockito.mock(CacheWriteAction.class);
        Map<String, Collection<? extends DtpRwdObjectDTO>> sourcesByKey =
                new LinkedHashMap<>();
        sourcesByKey.put("key-1", Collections.singletonList(firstSource));
        sourcesByKey.put("key-2", Collections.singletonList(secondSource));

        Assertions.assertTrue(service.replaceModelCacheKeys(
                sourcesByKey, Collections.singletonList(relationAction)));

        InOrder inOrder = Mockito.inOrder(
                objectConverter, modelViewConverter, transactionManager,
                objectRepository, modelViewRepository, relationAction);
        inOrder.verify(objectConverter).toCacheObject(Mockito.anyMap());
        inOrder.verify(modelViewConverter).toModelView(Mockito.anyMap());
        inOrder.verify(objectConverter).toCacheObject(Mockito.anyMap());
        inOrder.verify(modelViewConverter).toModelView(Mockito.anyMap());
        inOrder.verify(transactionManager).getTransaction(
                Mockito.any(TransactionDefinition.class));
        inOrder.verify(objectRepository).upsertObjects(
                Collections.singletonList(firstObject));
        inOrder.verify(modelViewRepository).upsertModelViews(
                Collections.singletonList(firstView));
        inOrder.verify(objectRepository).replaceObjectIds(
                "key-1", Collections.singletonList("object-1"));
        inOrder.verify(objectRepository).upsertObjects(
                Collections.singletonList(secondObject));
        inOrder.verify(modelViewRepository).upsertModelViews(
                Collections.singletonList(secondView));
        inOrder.verify(objectRepository).replaceObjectIds(
                "key-2", Collections.singletonList("object-2"));
        inOrder.verify(relationAction).execute();
        inOrder.verify(transactionManager).commit(transactionStatus);
        Mockito.verifyNoInteractions(relationRepository);
    }

    /**
     * 验证项目结构对象无 Key 完整替换动作的删除和写入顺序。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldReplaceObjectClassCodesWithoutCacheKey() {
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        CacheObjectRecordConverter objectConverter =
                Mockito.mock(CacheObjectRecordConverter.class);
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        DtpRwdObjectDTO sourceObject = new DtpRwdObjectDTO();
        sourceObject.setId("building-1");
        CacheObject object = new CacheObject();
        object.setId("building-1");
        object.setClassCode("BUILDING");
        CacheObjectModelView modelView = new CacheObjectModelView();
        modelView.setId("building-1");
        modelView.setClassCode("BUILDING");
        Mockito.when(objectConverter.toCacheObject(Mockito.anyMap())).thenReturn(object);
        Mockito.when(modelViewConverter.toModelView(Mockito.anyMap())).thenReturn(modelView);
        PlatformTransactionManager transactionManager =
                Mockito.mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = Mockito.mock(TransactionStatus.class);
        Mockito.when(transactionManager.getTransaction(
                        Mockito.any(TransactionDefinition.class)))
                .thenReturn(transactionStatus);
        CacheObjectModelWriteServiceImpl service = new CacheObjectModelWriteServiceImpl(
                objectRepository,
                modelViewRepository,
                Mockito.mock(CacheObjectRelRepository.class),
                objectConverter,
                modelViewConverter,
                transactionManager, this.localLocker());
        InOrder inOrder = Mockito.inOrder(objectRepository, modelViewRepository);
        Assertions.assertTrue(service.replaceObjectClassCodes(
                Collections.singletonList("BUILDING"),
                Collections.singletonList(sourceObject)));

        inOrder.verify(objectRepository).removeObjectsByClassCodes(
                Mockito.argThat(classCodes -> classCodes.equals(
                        Collections.singleton("BUILDING"))));
        inOrder.verify(modelViewRepository).removeModelViewsByClassCodes(
                Mockito.argThat(classCodes -> classCodes.equals(
                        Collections.singleton("BUILDING"))));
        inOrder.verify(objectRepository).upsertObjects(
                Collections.singletonList(object));
        inOrder.verify(modelViewRepository).upsertModelViews(
                Collections.singletonList(modelView));
        Mockito.verify(transactionManager).commit(transactionStatus);
    }

    /**
     * 验证项目结构替换动作不会接收声明范围之外的对象。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldRejectObjectClassOutsideReplacementScope() {
        CacheObjectRecordConverter objectConverter =
                Mockito.mock(CacheObjectRecordConverter.class);
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        CacheObject object = new CacheObject();
        object.setId("space-1");
        object.setClassCode("SPACE");
        Mockito.when(objectConverter.toCacheObject(Mockito.anyMap())).thenReturn(object);
        Mockito.when(modelViewConverter.toModelView(Mockito.anyMap()))
                .thenReturn(new CacheObjectModelView());
        CacheObjectModelWriteServiceImpl service = new CacheObjectModelWriteServiceImpl(
                Mockito.mock(CacheObjectRepository.class),
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectRelRepository.class),
                objectConverter,
                modelViewConverter,
                Mockito.mock(PlatformTransactionManager.class),
                this.localLocker());

        Assertions.assertThrows(
                BusinessException.class,
                () -> service.replaceObjectClassCodes(
                        Collections.singletonList("BUILDING"),
                        Collections.singletonList(new DtpRwdObjectDTO())));
    }

    /**
     * 验证第二个 Key 写入失败时异常上抛并回滚第一 Key 已执行的 SQL。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldRollbackAllKeysWhenSecondKeyWriteFails() {
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        PlatformTransactionManager transactionManager =
                Mockito.mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = Mockito.mock(TransactionStatus.class);
        Mockito.when(transactionManager.getTransaction(
                        Mockito.any(TransactionDefinition.class)))
                .thenReturn(transactionStatus);
        CacheObject firstObject = new CacheObject();
        firstObject.setId("object-1");
        CacheObject secondObject = new CacheObject();
        secondObject.setId("object-2");
        CacheObjectRecordConverter objectConverter =
                Mockito.mock(CacheObjectRecordConverter.class);
        Mockito.when(objectConverter.toCacheObject(Mockito.anyMap()))
                .thenReturn(firstObject, secondObject);
        CacheObjectModelView firstView = new CacheObjectModelView();
        CacheObjectModelView secondView = new CacheObjectModelView();
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        Mockito.when(modelViewConverter.toModelView(Mockito.anyMap()))
                .thenReturn(firstView, secondView);
        IllegalStateException failure = new IllegalStateException("second key failed");
        Mockito.doThrow(failure).when(objectRepository)
                .replaceObjectIds("key-2", Collections.singletonList("object-2"));
        CacheWriteAction relationAction = Mockito.mock(CacheWriteAction.class);
        CacheObjectModelWriteServiceImpl service = new CacheObjectModelWriteServiceImpl(
                objectRepository, modelViewRepository,
                Mockito.mock(CacheObjectRelRepository.class),
                objectConverter, modelViewConverter, transactionManager, this.localLocker());
        Map<String, Collection<? extends DtpRwdObjectDTO>> sourcesByKey =
                new LinkedHashMap<>();
        sourcesByKey.put("key-1", Collections.singletonList(new DtpRwdObjectDTO()));
        sourcesByKey.put("key-2", Collections.singletonList(new DtpRwdObjectDTO()));

        IllegalStateException actual = Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.replaceModelCacheKeys(
                        sourcesByKey, Collections.singletonList(relationAction)));

        Assertions.assertSame(failure, actual);
        Mockito.verify(objectRepository).replaceObjectIds(
                "key-1", Collections.singletonList("object-1"));
        Mockito.verify(objectRepository).replaceObjectIds(
                "key-2", Collections.singletonList("object-2"));
        Mockito.verifyNoInteractions(relationAction);
        Mockito.verify(transactionManager).rollback(transactionStatus);
        Mockito.verify(transactionManager, Mockito.never()).commit(transactionStatus);
    }

    /**
     * 验证多 Key 完成后关系动作失败会回滚全部对象、Key 和关系变更。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldRollbackAllKeysWhenPreparedRelationActionFails() {
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        PlatformTransactionManager transactionManager =
                Mockito.mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = Mockito.mock(TransactionStatus.class);
        Mockito.when(transactionManager.getTransaction(
                        Mockito.any(TransactionDefinition.class)))
                .thenReturn(transactionStatus);
        CacheObject firstObject = new CacheObject();
        firstObject.setId("object-1");
        CacheObject secondObject = new CacheObject();
        secondObject.setId("object-2");
        CacheObjectRecordConverter objectConverter =
                Mockito.mock(CacheObjectRecordConverter.class);
        Mockito.when(objectConverter.toCacheObject(Mockito.anyMap()))
                .thenReturn(firstObject, secondObject);
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        Mockito.when(modelViewConverter.toModelView(Mockito.anyMap()))
                .thenReturn(new CacheObjectModelView(), new CacheObjectModelView());
        IllegalStateException failure = new IllegalStateException("relation failed");
        CacheWriteAction relationAction = () -> {
            throw failure;
        };
        CacheObjectModelWriteServiceImpl service = new CacheObjectModelWriteServiceImpl(
                objectRepository, modelViewRepository,
                Mockito.mock(CacheObjectRelRepository.class),
                objectConverter, modelViewConverter, transactionManager, this.localLocker());
        Map<String, Collection<? extends DtpRwdObjectDTO>> sourcesByKey =
                new LinkedHashMap<>();
        sourcesByKey.put("key-1", Collections.singletonList(new DtpRwdObjectDTO()));
        sourcesByKey.put("key-2", Collections.singletonList(new DtpRwdObjectDTO()));

        IllegalStateException actual = Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.replaceModelCacheKeys(
                        sourcesByKey, Collections.singletonList(relationAction)));

        Assertions.assertSame(failure, actual);
        Mockito.verify(objectRepository).replaceObjectIds(
                "key-1", Collections.singletonList("object-1"));
        Mockito.verify(objectRepository).replaceObjectIds(
                "key-2", Collections.singletonList("object-2"));
        Mockito.verify(transactionManager).rollback(transactionStatus);
        Mockito.verify(transactionManager, Mockito.never()).commit(transactionStatus);
    }

    /**
     * 验证写入实现只持有本地仓储、投影转换器和事务管理器，不依赖远程查询。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldNotDependOnRemoteResources() {
        Class<?>[] constructorParameterTypes =
                CacheObjectModelWriteServiceImpl.class
                        .getDeclaredConstructors()[0]
                        .getParameterTypes();

        Assertions.assertArrayEquals(
                new Class<?>[]{
                        CacheObjectRepository.class,
                        CacheObjectModelViewRepository.class,
                        CacheObjectRelRepository.class,
                        CacheObjectRecordConverter.class,
                        CacheObjectModelViewRecordConverter.class,
                        PlatformTransactionManager.class,
                        RedissonDistributedLockerService.class
                },
                constructorParameterTypes);
    }
}
