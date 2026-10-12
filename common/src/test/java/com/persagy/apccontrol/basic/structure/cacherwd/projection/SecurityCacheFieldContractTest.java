package com.persagy.apccontrol.basic.structure.cacherwd.projection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 安防对象字段、模型视图字段和对外转换契约测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class SecurityCacheFieldContractTest {

    /**
     * 安防固定十二类设备。
     *
     * @since v7.260802.0
     */
    private static final List<DtpRwdClassCodeEnum> SECURITY_CLASS_CODES = Arrays.asList(
            DtpRwdClassCodeEnum.CFCSDR,
            DtpRwdClassCodeEnum.CFCSFR,
            DtpRwdClassCodeEnum.SPEGFG,
            DtpRwdClassCodeEnum.SPVSCM,
            DtpRwdClassCodeEnum.WEGMCM,
            DtpRwdClassCodeEnum.SPIACU,
            DtpRwdClassCodeEnum.SPIAGD,
            DtpRwdClassCodeEnum.SPIAID,
            DtpRwdClassCodeEnum.SPIAMA,
            DtpRwdClassCodeEnum.SPEGCU,
            DtpRwdClassCodeEnum.SPVSVE,
            DtpRwdClassCodeEnum.SPVSDR);

    /**
     * 项目 ObjectMapper 静态字段。
     */
    private Field objectMapperField;

    /**
     * 测试前的项目 ObjectMapper。
     */
    private Object originalObjectMapper;

    /**
     * 使用独立 ObjectMapper 验证 JSON 字段投影。
     *
     * @throws Exception 反射访问失败
     * @since v7.260802.0
     */
    @BeforeEach
    public void setUpObjectMapper() throws Exception {
        this.objectMapperField = ObjectMapperUtils.class.getDeclaredField("objectMapper");
        this.objectMapperField.setAccessible(true);
        this.originalObjectMapper = this.objectMapperField.get(null);
        this.objectMapperField.set(null, new ObjectMapper());
    }

    /**
     * 恢复项目 ObjectMapper。
     *
     * @throws Exception 反射访问失败
     * @since v7.260802.0
     */
    @AfterEach
    public void restoreObjectMapper() throws Exception {
        this.objectMapperField.set(null, this.originalObjectMapper);
    }

    /**
     * 验证十二类安防设备的模型视图字段严格为 BIM 标识和位置。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldExposeExactlyTwoSecurityModelViewFields() {
        Set<String> expected = new LinkedHashSet<>(Arrays.asList(
                SecurityDtpRwdEquipmentDTO.Fields.bimId,
                SecurityDtpRwdEquipmentDTO.Fields.bimLocation));

        for (DtpRwdClassCodeEnum classCode : SECURITY_CLASS_CODES) {
            Assertions.assertEquals(
                    expected,
                    CacheObjectExtensionFieldConfig.getModelViewFields(
                            classCode.value()),
                    classCode.value());
            Assertions.assertTrue(Collections.disjoint(
                    CacheObjectExtensionFieldConfig.getObjectFields(classCode.value()),
                    expected), classCode.value());
        }
    }

    /**
     * 验证全部模型视图配置：设备只有 BIM 标识和位置，空间额外保留功能和轮廓字段。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldRestrictEveryConfiguredModelViewByObjectKind() {
        Set<String> equipmentFields = new LinkedHashSet<>(Arrays.asList(
                DtpRwdEquipmentDTO.Fields.bimId,
                DtpRwdEquipmentDTO.Fields.bimLocation));
        Set<String> spaceFields = new LinkedHashSet<>(Arrays.asList(
                DtpRwdSpaceDTO.Fields.roomFuncType,
                DtpRwdSpaceDTO.Fields.bimId,
                DtpRwdSpaceDTO.Fields.bimLocation,
                DtpRwdSpaceDTO.Fields.outline));
        Set<String> spaceClassCodes = new LinkedHashSet<>(Arrays.asList(
                DtpRwdClassCodeEnum.SPACE.value(),
                DtpRwdClassCodeEnum.AC_SPACE.value()));

        CacheObjectExtensionFieldConfig.getModelViewFields().forEach(
                (classCode, fields) -> Assertions.assertEquals(
                        spaceClassCodes.contains(classCode) ? spaceFields : equipmentFields,
                        fields,
                        classCode));
        for (String spaceClassCode : spaceClassCodes) {
            Assertions.assertEquals(
                    spaceFields,
                    CacheObjectExtensionFieldConfig.getModelViewFields(spaceClassCode),
                    spaceClassCode);
        }
    }

    /**
     * 验证两类摄像头都保留旧设备接口图标判断消费的静态字段。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldKeepCameraDecisionFieldsForBothSecurityCameraClasses() {
        Set<String> expected = new LinkedHashSet<>(Arrays.asList(
                SecurityDtpRwdEquipmentDTO.Fields.hasArmingFunc,
                SecurityDtpRwdEquipmentDTO.Fields.cameraType,
                InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode()));

        Assertions.assertTrue(CacheObjectExtensionFieldConfig.getObjectFields(
                DtpRwdClassCodeEnum.SPVSCM.value()).containsAll(expected));
        Assertions.assertTrue(CacheObjectExtensionFieldConfig.getObjectFields(
                DtpRwdClassCodeEnum.WEGMCM.value()).containsAll(expected));
    }

    /**
     * 验证安防设备和空间只查询 localName，不查询或持久化 name。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldUseLocalNameWithoutPersistingName() {
        Assertions.assertTrue(CacheObjectExtensionFieldConfig
                .getForbiddenExtensionFields().contains(
                        DtpRwdObjectDTO.Fields.name));
        CacheObjectExtensionFieldConfig.getObjectExtensionFields().values()
                .forEach(fields -> Assertions.assertFalse(fields.contains(
                        DtpRwdObjectDTO.Fields.name)));
        for (DtpRwdClassCodeEnum classCode : SECURITY_CLASS_CODES) {
            Assertions.assertFalse(CacheObjectExtensionFieldConfig.getObjectFields(
                    classCode.value()).contains(DtpRwdObjectDTO.Fields.name),
                    classCode.value());
            Assertions.assertFalse(CacheObjectExtensionFieldConfig.listRwdQueryFields(
                    classCode.value()).contains(DtpRwdObjectDTO.Fields.name),
                    classCode.value());
            Assertions.assertTrue(CacheObjectExtensionFieldConfig.listRwdQueryFields(
                    classCode.value()).contains(DtpRwdObjectDTO.Fields.localName),
                    classCode.value());
        }
        Assertions.assertFalse(CacheObjectExtensionFieldConfig.getObjectFields(
                DtpRwdClassCodeEnum.SPACE.value()).contains(
                DtpRwdObjectDTO.Fields.name));
        Assertions.assertTrue(CacheObjectExtensionFieldConfig.listRwdQueryFields(
                DtpRwdClassCodeEnum.SPACE.value()).contains(
                DtpRwdObjectDTO.Fields.localName));
    }

    /**
     * 验证安防对象 JSON 忽略 name，并仅从独立基础列恢复 localName。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldRebuildSecurityLocalNameWithoutName() {
        CacheObjectExtensionProjector projector = new CacheObjectExtensionProjector();
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put(DtpRwdObjectDTO.Fields.name, "RWD Name");
        fields.put(DtpRwdObjectDTO.Fields.localName, "Local Name");

        String objectJson = projector.projectObject(
                DtpRwdClassCodeEnum.SPVSCM.value(), fields);
        Map<String, Object> objectFields = ObjectMapperUtils.readMapFailSafe(objectJson);
        SecurityDtpRwdEquipmentDTO rebuilt = projector.rebuildObject(
                DtpRwdClassCodeEnum.SPVSCM.value(),
                Collections.singletonMap(DtpRwdObjectDTO.Fields.localName, "Local Name"),
                objectJson,
                Arrays.asList(
                        DtpRwdObjectDTO.Fields.name,
                        DtpRwdObjectDTO.Fields.localName),
                SecurityDtpRwdEquipmentDTO.class);

        Assertions.assertNotNull(objectFields);
        Assertions.assertFalse(objectFields.containsKey(DtpRwdObjectDTO.Fields.name));
        Assertions.assertNotNull(rebuilt);
        Assertions.assertNull(rebuilt.getName());
        Assertions.assertEquals("Local Name", rebuilt.getLocalName());
    }

    /**
     * 验证模型图字段只进入模型视图 JSON，不进入对象 JSON。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldKeepSecurityModelFieldsOutOfObjectJson() {
        CacheObjectExtensionProjector projector = new CacheObjectExtensionProjector();
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put(SecurityDtpRwdEquipmentDTO.Fields.hasArmingFunc, "1");
        fields.put(SecurityDtpRwdEquipmentDTO.Fields.bimId, "bim-1");
        fields.put(SecurityDtpRwdEquipmentDTO.Fields.bimLocation, "location-1");
        fields.put(DtpRwdSpaceDTO.Fields.outline, "outline-1");

        Map<String, Object> objectFields = ObjectMapperUtils.readMapFailSafe(
                projector.projectObject(DtpRwdClassCodeEnum.SPVSCM.value(), fields));
        Map<String, Object> modelFields = ObjectMapperUtils.readMapFailSafe(
                projector.projectModelView(DtpRwdClassCodeEnum.SPVSCM.value(), fields));

        Assertions.assertNotNull(objectFields);
        Assertions.assertEquals("1", objectFields.get(
                SecurityDtpRwdEquipmentDTO.Fields.hasArmingFunc));
        Assertions.assertFalse(objectFields.containsKey(
                SecurityDtpRwdEquipmentDTO.Fields.bimId));
        Assertions.assertFalse(objectFields.containsKey(
                SecurityDtpRwdEquipmentDTO.Fields.bimLocation));
        Assertions.assertFalse(objectFields.containsKey(
                DtpRwdSpaceDTO.Fields.outline));
        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList(
                SecurityDtpRwdEquipmentDTO.Fields.bimId,
                SecurityDtpRwdEquipmentDTO.Fields.bimLocation)),
                modelFields.keySet());
        Assertions.assertFalse(modelFields.containsKey(DtpRwdSpaceDTO.Fields.outline));
    }

}
