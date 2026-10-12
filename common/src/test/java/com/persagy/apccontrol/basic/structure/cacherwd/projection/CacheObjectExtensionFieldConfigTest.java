package com.persagy.apccontrol.basic.structure.cacherwd.projection;

import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link CacheObjectExtensionFieldConfig#listAllRwdQueryFields()} 全量并集契约测试。
 *
 * <p>并集必须与基础字段、全部对象扩展字段、全部模型视图字段及通用字段之和完全相等：
 * 防漏（字段子集写入会在 UPSERT 整列覆盖时丢字段）、防多（未登记字段下发扩大旧接口响应）。</p>
 *
 * @author JiYinchuan
 * @since v7.260802.3
 */
public class CacheObjectExtensionFieldConfigTest {

    /**
     * 验证全量并集与各登记来源之和完全相等，且每个已登记 classCode 的白名单均被覆盖。
     *
     * @since v7.260802.3
     */
    @Test
    public void shouldListFullUnionOfAllRegisteredFields() {
        Set<String> expected = new LinkedHashSet<>(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(null));
        CacheObjectExtensionFieldConfig.getObjectExtensionFields().values()
                .forEach(expected::addAll);
        CacheObjectExtensionFieldConfig.getModelViewFields().values()
                .forEach(expected::addAll);

        Set<String> union = new LinkedHashSet<>(
                CacheObjectExtensionFieldConfig.listAllRwdQueryFields());

        Assertions.assertEquals(expected, union);
        CacheObjectExtensionFieldConfig.getObjectExtensionFields().keySet()
                .forEach(classCode -> Assertions.assertTrue(
                        union.containsAll(CacheObjectExtensionFieldConfig.listRwdQueryFields(classCode)),
                        classCode));
        CacheObjectExtensionFieldConfig.getModelViewFields().keySet()
                .forEach(classCode -> Assertions.assertTrue(
                        union.containsAll(CacheObjectExtensionFieldConfig.listRwdQueryFields(classCode)),
                        classCode));
    }

    /**
     * 验证通用字段与照明回路业务字段包含在全量并集中。
     *
     * @since v7.260802.3
     */
    @Test
    public void shouldContainUniversalAndBusinessFields() {
        Set<String> union = CacheObjectExtensionFieldConfig.listAllRwdQueryFields();

        Assertions.assertTrue(union.containsAll(Arrays.asList(
                InfoCodeEnum.MAJOR.getCode(),
                DtpRwdEquipmentDTO.Fields.bimId,
                DtpRwdEquipmentDTO.Fields.bimLocation)));
        Assertions.assertTrue(union.containsAll(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(
                        DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value())));
    }
}
