package com.persagy.apccontrol.basic.structure.cacherwd.projection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheTemplateObjectDetail;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeGroupVO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * 模板对象明细持久化 JSON 转换器测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheTemplateObjectDetailRecordConverterTest {

    /**
     * 用于替换项目级 ObjectMapper 的反射字段。
     *
     * @since v7.260701.0
     */
    private Field objectMapperField;

    /**
     * 测试前保存的项目级 ObjectMapper 实例。
     *
     * @since v7.260701.0
     */
    private Object originalObjectMapper;

    /**
     * 为纯单元测试安装独立 ObjectMapper。
     *
     * @throws Exception 反射访问项目级 ObjectMapper 字段失败时抛出
     * @since v7.260701.0
     */
    @BeforeEach
    public void setUp() throws Exception {
        this.objectMapperField = ObjectMapperUtils.class.getDeclaredField("objectMapper");
        this.objectMapperField.setAccessible(true);
        this.originalObjectMapper = this.objectMapperField.get(null);
        this.objectMapperField.set(null, new ObjectMapper());
    }

    /**
     * 恢复测试前的项目级 ObjectMapper。
     *
     * @throws Exception 反射恢复项目级 ObjectMapper 字段失败时抛出
     * @since v7.260701.0
     */
    @AfterEach
    public void tearDown() throws Exception {
        this.objectMapperField.set(null, this.originalObjectMapper);
    }

    /**
     * 验证任意深度关联对象的请求态配置都会被剥离且源 DTO 不被修改。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldRemoveRequestScopedConfigsRecursivelyWithoutMutatingSource() {
        InfoCodeRwdObjectDetailDTO grandchild = this.detail("grandchild");
        InfoCodeRwdObjectDetailDTO child = this.detail("child");
        child.setRelObjects(Collections.singletonList(grandchild));
        InfoCodeRwdObjectDetailDTO root = this.detail("root");
        root.setRelObjects(Collections.singletonList(child));
        CacheTemplateObjectDetailRecordConverter converter =
                new CacheTemplateObjectDetailRecordConverter();

        CacheTemplateObjectDetail detail = converter.toDetail(
                "template-1", "root", "info-code", Collections.singletonList(root));
        List<InfoCodeRwdObjectDetailDTO> restored = converter.toResult(detail);

        Assertions.assertNotNull(restored);
        Assertions.assertEquals(1, restored.size());
        Assertions.assertFalse(detail.getResultJson().contains("relInfoCodeGroupConfigs"));
        Assertions.assertNotNull(root.getRelInfoCodeGroupConfigs());
        Assertions.assertNotNull(child.getRelInfoCodeGroupConfigs());
        Assertions.assertNotNull(grandchild.getRelInfoCodeGroupConfigs());
        Assertions.assertNull(restored.get(0).getRelInfoCodeGroupConfigs());
        Assertions.assertNull(restored.get(0).getRelObjects().get(0).getRelInfoCodeGroupConfigs());
        Assertions.assertNull(restored.get(0).getRelObjects().get(0)
                .getRelObjects().get(0).getRelInfoCodeGroupConfigs());
    }

    /**
     * 验证持久化的空数组是有效命中并可往返为空列表。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldRoundTripPersistedEmptyResult() {
        CacheTemplateObjectDetailRecordConverter converter =
                new CacheTemplateObjectDetailRecordConverter();

        CacheTemplateObjectDetail detail = converter.toDetail(
                "template-1", "object-1", "info-code", Collections.emptyList());

        Assertions.assertEquals("[]", detail.getResultJson());
        Assertions.assertEquals(Collections.emptyList(), converter.toResult(detail));
    }

    /**
     * 验证损坏的持久化 JSON 经 fail-safe 转换后交给读取边界按未命中处理。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReturnNullForMalformedPersistedResult() {
        CacheTemplateObjectDetail detail = new CacheTemplateObjectDetail();
        detail.setResultJson("{broken");

        List<InfoCodeRwdObjectDetailDTO> result =
                new CacheTemplateObjectDetailRecordConverter().toResult(detail);

        Assertions.assertNull(result);
    }

    /**
     * 创建带请求态信息点配置的明细节点。
     *
     * @param id 对象 ID
     * @return 明细节点
     * @since v7.260701.0
     */
    private InfoCodeRwdObjectDetailDTO detail(String id) {
        InfoCodeRwdObjectDetailDTO detail = new InfoCodeRwdObjectDetailDTO();
        detail.setId(id);
        detail.setRelInfoCodeGroupConfigs(Collections.singletonList(new InfoCodeGroupVO()));
        return detail;
    }
}
