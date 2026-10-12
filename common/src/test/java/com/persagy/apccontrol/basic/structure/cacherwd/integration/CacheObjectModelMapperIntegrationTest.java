package com.persagy.apccontrol.basic.structure.cacherwd.integration;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.persagy.apccontrol.basic.mapper.cache.CacheObjectKeyRelMapper;
import com.persagy.apccontrol.basic.mapper.cache.CacheObjectMapper;
import com.persagy.apccontrol.basic.mapper.cache.CacheObjectModelViewMapper;
import com.persagy.apccontrol.basic.mapper.cache.CacheObjectRelMapper;
import com.persagy.apccontrol.basic.mapper.cache.CacheRuleKeyRelMapper;
import com.persagy.apccontrol.basic.mapper.cache.CacheRuleMapper;
import com.persagy.apccontrol.basic.mapper.cache.CacheTemplateObjectDetailMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectKeyRel;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.entity.cache.CacheRule;
import com.persagy.apccontrol.basic.model.entity.cache.CacheRuleKeyRel;
import com.persagy.apccontrol.basic.model.entity.cache.CacheTemplateObjectDetail;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectQueryDaoParam;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelReplaceScope;
import com.persagy.apccontrol.basic.resource.cache.param.CacheRuleBusinessKey;
import com.persagy.apccontrol.basic.resource.cache.param.CacheTemplateObjectDetailBusinessKey;
import com.persagy.apccontrol.basic.resource.cache.param.CacheTemplateObjectDetailQueryDaoParam;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 缓存对象模型七表 Mapper 和事务边界集成测试。
 *
 * <p>测试通过 {@code cache.object.model.jdbc.*} 系统属性连接隔离 MySQL。未提供连接属性时
 * 自动跳过，普通单元测试不依赖开发机数据库；最终集成回归通过临时 MySQL 容器显式启用。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheObjectModelMapperIntegrationTest {

    /**
     * JDBC URL 系统属性。
     *
     * @since v7.260701.0
     */
    private static final String JDBC_URL_PROPERTY = "cache.object.model.jdbc.url";

    /**
     * JDBC 用户名系统属性。
     *
     * @since v7.260701.0
     */
    private static final String JDBC_USERNAME_PROPERTY = "cache.object.model.jdbc.username";

    /**
     * JDBC 密码系统属性。
     *
     * @since v7.260701.0
     */
    private static final String JDBC_PASSWORD_PROPERTY = "cache.object.model.jdbc.password";

    /**
     * 当前集成测试固定使用的项目 ID。
     *
     * @since v7.260701.0
     */
    private static final String PROJECT_ID = "mapper-test-project";

    /**
     * 测试使用的固定审计时间。
     *
     * @since v7.260701.0
     */
    private static final LocalDateTime AUDIT_TIME = LocalDateTime.of(2026, 7, 15, 12, 0);

    /**
     * 按外键依赖逆序清理测试项目数据的参数化 SQL。
     *
     * @since v7.260701.0
     */
    private static final List<String> DELETE_PROJECT_SQL = Collections.unmodifiableList(Arrays.asList(
            "delete from cache_rule_key_rel where project_id = ?",
            "delete from cache_rule where project_id = ?",
            "delete from cache_template_object_detail where project_id = ?",
            "delete from cache_object_rel where project_id = ?",
            "delete from cache_object_key_rel where project_id = ?",
            "delete from cache_object_model_view where project_id = ?",
            "delete from cache_object where project_id = ?"));

    /**
     * 集成测试数据源。
     *
     * @since v7.260701.0
     */
    private static PooledDataSource dataSource;

    /**
     * 手工加载七张缓存表 Mapper XML 后构建的 MyBatis 会话工厂。
     *
     * @since v7.260701.0
     */
    private static SqlSessionFactory sqlSessionFactory;

    /**
     * 在显式提供数据库连接时构建 MyBatis 会话工厂。
     *
     * @since v7.260701.0
     */
    @BeforeAll
    public static void setUp() {
        String url = System.getProperty(JDBC_URL_PROPERTY);
        String username = System.getProperty(JDBC_USERNAME_PROPERTY);
        String password = System.getProperty(JDBC_PASSWORD_PROPERTY);
        Assumptions.assumeTrue(url != null && username != null && password != null,
                "cache object model mapper integration database is not configured");
        CacheObjectModelMapperIntegrationTest.dataSource = new PooledDataSource(
                "com.mysql.cj.jdbc.Driver", url, username, password);
        MybatisConfiguration configuration = new MybatisConfiguration(
                new Environment(
                        "cache-object-model-integration",
                        new JdbcTransactionFactory(),
                        CacheObjectModelMapperIntegrationTest.dataSource));
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(CacheObjectMapper.class);
        configuration.addMapper(CacheObjectKeyRelMapper.class);
        configuration.addMapper(CacheObjectModelViewMapper.class);
        configuration.addMapper(CacheObjectRelMapper.class);
        configuration.addMapper(CacheTemplateObjectDetailMapper.class);
        configuration.addMapper(CacheRuleMapper.class);
        configuration.addMapper(CacheRuleKeyRelMapper.class);
        CacheObjectModelMapperIntegrationTest.parseMapperXml(configuration);
        CacheObjectModelMapperIntegrationTest.sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * 清空本测试项目的数据，保证每个用例只观察自己的事务结果。
     *
     * @throws SQLException 清理数据库失败
     * @since v7.260701.0
     */
    @BeforeEach
    public void clearProjectRows() throws SQLException {
        try (Connection connection = CacheObjectModelMapperIntegrationTest.dataSource.getConnection()) {
            for (String deleteSql : CacheObjectModelMapperIntegrationTest.DELETE_PROJECT_SQL) {
                try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
                    statement.setString(1, CacheObjectModelMapperIntegrationTest.PROJECT_ID);
                    statement.executeUpdate();
                }
            }
        }
    }

    /**
     * 通过七个 Mapper 完成对象、视图、关系、明细和规则的批量写入与精确读取。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldPersistAndQueryAllCacheObjectModelTables() {
        try (SqlSession session = CacheObjectModelMapperIntegrationTest.sqlSessionFactory.openSession(false)) {
            CacheObjectMapper objectMapper = session.getMapper(CacheObjectMapper.class);
            CacheObjectKeyRelMapper keyRelMapper = session.getMapper(CacheObjectKeyRelMapper.class);
            CacheObjectModelViewMapper viewMapper = session.getMapper(CacheObjectModelViewMapper.class);
            CacheObjectRelMapper relationMapper = session.getMapper(CacheObjectRelMapper.class);
            CacheTemplateObjectDetailMapper detailMapper = session.getMapper(
                    CacheTemplateObjectDetailMapper.class);
            CacheRuleMapper ruleMapper = session.getMapper(CacheRuleMapper.class);
            CacheRuleKeyRelMapper ruleKeyRelMapper = session.getMapper(CacheRuleKeyRelMapper.class);

            objectMapper.upsertBatch(Collections.singletonList(this.object("object-1")));
            viewMapper.upsertBatch(Collections.singletonList(this.modelView("object-1")));
            keyRelMapper.insertBatch(Collections.singletonList(this.objectRelation("cache-key-1", "object-1")));
            relationMapper.insertBatch(Collections.singletonList(this.relation("object-1", "object-2")));
            detailMapper.insertBatch(Collections.singletonList(this.detail("object-1")));
            ruleMapper.upsertBatch(Collections.singletonList(this.rule(401L, "object-1")));
            ruleKeyRelMapper.insertBatch(Collections.singletonList(this.ruleRelation(501L, 401L)));

            CacheObjectQueryDaoParam objectParam = new CacheObjectQueryDaoParam()
                    .setIds(Collections.singletonList("object-1"))
                    .setMonitoringSurveillanceStatusCodes(Collections.singletonList("3"));
            Assertions.assertEquals(1, objectMapper.list(PROJECT_ID, objectParam).size());
            Assertions.assertEquals(1, viewMapper.list(PROJECT_ID,
                    new CacheObjectModelViewQueryDaoParam().setIds(Collections.singletonList("object-1")))
                    .size());
            Assertions.assertEquals(1, keyRelMapper.listByCacheKeys(
                    PROJECT_ID, Collections.singletonList("cache-key-1")).size());
            Assertions.assertEquals(1, relationMapper.listByFrom(PROJECT_ID,
                    new CacheObjectRelQueryDaoParam().setObjectIds(Collections.singletonList("object-1")))
                    .size());
            Assertions.assertEquals(1, detailMapper.listByBusinessKeys(PROJECT_ID,
                    new CacheTemplateObjectDetailQueryDaoParam().setBusinessKeys(Collections.singletonList(
                            new CacheTemplateObjectDetailBusinessKey("template-1", "object-1", "info-1"))))
                    .size());
            Assertions.assertEquals(1, ruleMapper.listByBusinessKeys(PROJECT_ID,
                    Collections.singletonList(new CacheRuleBusinessKey(PROJECT_ID, "object-1", "info-1",
                            CacheIotRuleTypeEnum.PERSISTENCE))).size());
            Assertions.assertEquals(Collections.singletonList(401L),
                    ruleKeyRelMapper.listRuleIdsByCacheKey(PROJECT_ID, "cache-key-1"));
            session.commit();
        }
    }

    /**
     * 关系范围替换只清理指定主体、图谱和关系编码，不影响同一主体的其他关系范围。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReplaceRelationScopesWithoutTouchingOtherScopes() {
        try (SqlSession session = CacheObjectModelMapperIntegrationTest.sqlSessionFactory.openSession(false)) {
            CacheObjectRelMapper mapper = session.getMapper(CacheObjectRelMapper.class);
            mapper.insertBatch(Arrays.asList(
                    this.relation("object-1", "object-2", "graph-1", "rel-1"),
                    this.relation("object-1", "object-3", "graph-2", "rel-2")));
            mapper.deleteScopes(PROJECT_ID, Collections.singletonList(
                    new CacheObjectRelReplaceScope("object-1", "graph-1", "rel-1",
                            Collections.singletonList("object-2"))));
            session.commit();
        }
        try (SqlSession session = CacheObjectModelMapperIntegrationTest.sqlSessionFactory.openSession(true)) {
            List<CacheObjectRel> relations = session.getMapper(CacheObjectRelMapper.class).listByFrom(
                    PROJECT_ID, new CacheObjectRelQueryDaoParam()
                            .setObjectIds(Collections.singletonList("object-1")));
            Assertions.assertEquals(1, relations.size());
            Assertions.assertEquals("object-3", relations.get(0).getRelObjectId());
        }
    }

    /**
     * 七表写入过程中发生约束异常时，之前已经执行的 Mapper 写入必须全部回滚。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldRollbackAllWritesWhenOneMapperFails() {
        try (SqlSession session = CacheObjectModelMapperIntegrationTest.sqlSessionFactory.openSession(false)) {
            CacheObjectMapper objectMapper = session.getMapper(CacheObjectMapper.class);
            CacheObjectKeyRelMapper keyRelMapper = session.getMapper(CacheObjectKeyRelMapper.class);
            CacheObjectModelViewMapper viewMapper = session.getMapper(CacheObjectModelViewMapper.class);
            CacheObjectRelMapper relationMapper = session.getMapper(CacheObjectRelMapper.class);
            CacheTemplateObjectDetailMapper detailMapper = session.getMapper(
                    CacheTemplateObjectDetailMapper.class);
            CacheRuleMapper ruleMapper = session.getMapper(CacheRuleMapper.class);
            CacheRuleKeyRelMapper ruleKeyRelMapper = session.getMapper(CacheRuleKeyRelMapper.class);
            objectMapper.upsertBatch(Collections.singletonList(this.object("object-rollback")));
            keyRelMapper.insertBatch(Collections.singletonList(
                    this.objectRelation("cache-key-1", "object-rollback")));
            viewMapper.upsertBatch(Collections.singletonList(this.modelView("object-rollback")));
            relationMapper.insertBatch(Collections.singletonList(
                    this.relation("object-rollback", "object-target")));
            detailMapper.insertBatch(Collections.singletonList(this.detail("object-rollback")));
            ruleMapper.upsertBatch(Collections.singletonList(this.rule(402L, "object-rollback")));
            ruleKeyRelMapper.insertBatch(Collections.singletonList(
                    this.ruleRelation(502L, 402L)));
            CacheTemplateObjectDetail invalidDetail = this.detail("object-rollback");
            invalidDetail.setId(302L);
            invalidDetail.setResultJson(null);
            Assertions.assertThrows(RuntimeException.class,
                    () -> detailMapper.insertBatch(Collections.singletonList(invalidDetail)));
            session.rollback();
        }
        try (SqlSession session = CacheObjectModelMapperIntegrationTest.sqlSessionFactory.openSession(true)) {
            Assertions.assertTrue(session.getMapper(CacheObjectMapper.class).list(PROJECT_ID,
                    new CacheObjectQueryDaoParam().setIds(Collections.singletonList("object-rollback")))
                    .isEmpty());
            Assertions.assertTrue(session.getMapper(CacheObjectKeyRelMapper.class).listByCacheKeys(
                    PROJECT_ID, Collections.singletonList("cache-key-1")).isEmpty());
            Assertions.assertTrue(session.getMapper(CacheObjectModelViewMapper.class).list(PROJECT_ID,
                    new CacheObjectModelViewQueryDaoParam()
                            .setIds(Collections.singletonList("object-rollback"))).isEmpty());
            Assertions.assertTrue(session.getMapper(CacheObjectRelMapper.class).listByFrom(PROJECT_ID,
                    new CacheObjectRelQueryDaoParam()
                            .setObjectIds(Collections.singletonList("object-rollback"))).isEmpty());
            Assertions.assertTrue(session.getMapper(CacheTemplateObjectDetailMapper.class)
                    .listByBusinessKeys(PROJECT_ID,
                            new CacheTemplateObjectDetailQueryDaoParam().setBusinessKeys(
                                    Collections.singletonList(new CacheTemplateObjectDetailBusinessKey(
                                            "template-1", "object-rollback", "info-1"))))
                    .isEmpty());
            Assertions.assertTrue(session.getMapper(CacheRuleMapper.class).listByBusinessKeys(PROJECT_ID,
                    Collections.singletonList(new CacheRuleBusinessKey(PROJECT_ID, "object-rollback",
                            "info-1", CacheIotRuleTypeEnum.PERSISTENCE))).isEmpty());
            Assertions.assertTrue(session.getMapper(CacheRuleKeyRelMapper.class)
                    .listRuleIdsByCacheKey(PROJECT_ID, "cache-key-1").isEmpty());
        }
    }

    /**
     * 关闭集成测试连接池。
     *
     * @since v7.260701.0
     */
    @AfterAll
    public static void tearDown() {
        if (CacheObjectModelMapperIntegrationTest.dataSource != null) {
            CacheObjectModelMapperIntegrationTest.dataSource.forceCloseAll();
        }
    }

    /**
     * 解析七张缓存表的手写 Mapper XML。
     *
     * @param configuration MyBatis 配置
     * @since v7.260701.0
     */
    private static void parseMapperXml(@NonNull MybatisConfiguration configuration) {
        List<String> resources = Arrays.asList(
                "mapper/cache/CacheObjectMapper.xml",
                "mapper/cache/CacheObjectKeyRelMapper.xml",
                "mapper/cache/CacheObjectModelViewMapper.xml",
                "mapper/cache/CacheObjectRelMapper.xml",
                "mapper/cache/CacheTemplateObjectDetailMapper.xml",
                "mapper/cache/CacheRuleMapper.xml",
                "mapper/cache/CacheRuleKeyRelMapper.xml");
        for (String resource : resources) {
            try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
                new XMLMapperBuilder(inputStream, configuration, resource,
                        configuration.getSqlFragments()).parse();
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to load cache mapper XML: " + resource, exception);
            }
        }
    }

    /**
     * 创建对象主表测试记录。
     *
     * @param objectId 对象 ID
     * @return 对象记录
     * @since v7.260701.0
     */
    private @NonNull CacheObject object(@NonNull String objectId) {
        CacheObject result = new CacheObject();
        result.setId(objectId);
        result.setProjectId(PROJECT_ID);
        result.setClassCode("class-code");
        result.setObjectType("SPACE");
        result.setMonitoringSurveillanceStatus("3");
        result.setObjectData("{\"bimId\":\"" + objectId + "\"}");
        this.audit(result);
        return result;
    }

    /**
     * 创建模型视图测试记录。
     *
     * @param objectId 对象 ID
     * @return 模型视图记录
     * @since v7.260701.0
     */
    private @NonNull CacheObjectModelView modelView(@NonNull String objectId) {
        CacheObjectModelView result = new CacheObjectModelView();
        result.setId(objectId);
        result.setProjectId(PROJECT_ID);
        result.setClassCode("class-code");
        result.setObjectType("SPACE");
        result.setViewData("{\"outline\":[]}");
        this.audit(result);
        return result;
    }

    /**
     * 创建缓存键与对象关联测试记录。
     *
     * @param cacheKey 缓存键
     * @param objectId 对象 ID
     * @return 对象关联记录
     * @since v7.260701.0
     */
    private @NonNull CacheObjectKeyRel objectRelation(
            @NonNull String cacheKey, @NonNull String objectId) {
        CacheObjectKeyRel result = new CacheObjectKeyRel();
        result.setId((long) (cacheKey.hashCode() & Integer.MAX_VALUE));
        result.setProjectId(PROJECT_ID);
        result.setCacheKey(cacheKey);
        result.setObjectId(objectId);
        this.audit(result);
        return result;
    }

    /**
     * 创建对象关系测试记录。
     *
     * @param objectId 主体对象 ID
     * @param relObjectId 目标对象 ID
     * @return 对象关系记录
     * @since v7.260701.0
     */
    private @NonNull CacheObjectRel relation(
            @NonNull String objectId, @NonNull String relObjectId) {
        return this.relation(objectId, relObjectId, "graph-1", "rel-1");
    }

    /**
     * 创建带指定关系范围的对象关系测试记录。
     *
     * @param objectId 主体对象 ID
     * @param relObjectId 目标对象 ID
     * @param graphCode 图谱编码
     * @param relCode 关系编码
     * @return 对象关系记录
     * @since v7.260701.0
     */
    private @NonNull CacheObjectRel relation(
            @NonNull String objectId, @NonNull String relObjectId,
            @NonNull String graphCode, @NonNull String relCode) {
        CacheObjectRel result = new CacheObjectRel();
        result.setId((long) ((objectId + graphCode + relCode + relObjectId).hashCode()
                & Integer.MAX_VALUE));
        result.setProjectId(PROJECT_ID);
        result.setObjectId(objectId);
        result.setGraphCode(graphCode);
        result.setRelCode(relCode);
        result.setRelObjectId(relObjectId);
        this.audit(result);
        return result;
    }

    /**
     * 创建模板对象明细测试记录。
     *
     * @param objectId 对象 ID
     * @return 明细记录
     * @since v7.260701.0
     */
    private @NonNull CacheTemplateObjectDetail detail(@NonNull String objectId) {
        CacheTemplateObjectDetail result = new CacheTemplateObjectDetail();
        result.setId(301L);
        result.setProjectId(PROJECT_ID);
        result.setTemplateKey("template-1");
        result.setObjectId(objectId);
        result.setInfoCode("info-1");
        result.setResultJson("[]");
        this.audit(result);
        return result;
    }

    /**
     * 创建规则主表测试记录。
     *
     * @param id 规则 ID
     * @param objectId 对象 ID
     * @return 规则记录
     * @since v7.260701.0
     */
    private @NonNull CacheRule rule(@NonNull Long id, @NonNull String objectId) {
        CacheRule result = new CacheRule();
        result.setId(id);
        result.setProjectId(PROJECT_ID);
        result.setObjectId(objectId);
        result.setInfoCode("info-1");
        result.setType(CacheIotRuleTypeEnum.PERSISTENCE);
        this.audit(result);
        return result;
    }

    /**
     * 创建规则键关联测试记录。
     *
     * @param id 关联 ID
     * @param ruleId 规则 ID
     * @return 规则键关联记录
     * @since v7.260701.0
     */
    private @NonNull CacheRuleKeyRel ruleRelation(@NonNull Long id, @NonNull Long ruleId) {
        CacheRuleKeyRel result = new CacheRuleKeyRel();
        result.setId(id);
        result.setProjectId(PROJECT_ID);
        result.setCacheKey("cache-key-1");
        result.setRuleId(ruleId);
        this.audit(result);
        return result;
    }

    /**
     * 填充七张表手写批量 SQL 所需的固定审计字段。
     *
     * @param entity 缓存实体
     * @since v7.260701.0
     */
    private void audit(@NonNull BaseDO entity) {
        entity.setCreateTime(AUDIT_TIME);
        entity.setUpdateTime(AUDIT_TIME);
    }
}
