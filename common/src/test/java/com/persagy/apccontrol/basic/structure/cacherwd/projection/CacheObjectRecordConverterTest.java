package com.persagy.apccontrol.basic.structure.cacherwd.projection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache object independent-column projection tests.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheObjectRecordConverterTest {

    /**
     * Project ObjectMapper holder used by the projection utilities.
     *
     * @since v7.260701.0
     */
    private Field objectMapperField;

    /**
     * ObjectMapper installed before this test.
     *
     * @since v7.260701.0
     */
    private Object originalObjectMapper;

    /**
     * Installs an isolated mapper for this pure unit test.
     *
     * @throws Exception when the shared mapper field cannot be accessed
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
     * Restores the shared mapper after the test.
     *
     * @throws Exception when the shared mapper field cannot be restored
     * @since v7.260701.0
     */
    @AfterEach
    public void tearDown() throws Exception {
        this.objectMapperField.set(null, this.originalObjectMapper);
    }

    /**
     * Verifies nullable base fields round-trip through independent columns and stay out of JSON.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldRoundTripRequestedBaseFieldsOutsideObjectData() {
        CacheObjectRecordConverter converter = new CacheObjectRecordConverter(
                new CacheObjectExtensionProjector());
        Map<String, Object> actualFields = new LinkedHashMap<>();
        actualFields.put(DtpRwdObjectDTO.Fields.id, "space-1");
        actualFields.put(DtpRwdObjectDTO.Fields.localId, "L-001");
        actualFields.put(DtpRwdObjectDTO.Fields.localName, "Local Space Name");
        actualFields.put(DtpRwdObjectDTO.Fields.classCode,
                DtpRwdClassCodeEnum.SPACE.value());
        actualFields.put(DtpRwdObjectDTO.Fields.objType,
                DtpRwdObjectTypeEnum.SPACE.value());

        CacheObject cacheObject = converter.toCacheObject(actualFields);

        Assertions.assertEquals("L-001", cacheObject.getLocalId());
        Assertions.assertEquals("Local Space Name", cacheObject.getLocalName());
        Map<String, Object> objectData = ObjectMapperUtils.readMapFailSafe(
                cacheObject.getObjectData());
        Assertions.assertNotNull(objectData);
        Assertions.assertFalse(objectData.containsKey(DtpRwdObjectDTO.Fields.localId));
        Assertions.assertFalse(objectData.containsKey(DtpRwdObjectDTO.Fields.localName));

        DtpRwdObjectDTO rebuilt = converter.toRwdObject(
                cacheObject,
                DtpRwdObjectDTO.class,
                Arrays.asList(
                        DtpRwdObjectDTO.Fields.id,
                        DtpRwdObjectDTO.Fields.localId,
                        DtpRwdObjectDTO.Fields.localName));
        Assertions.assertNotNull(rebuilt);
        Assertions.assertEquals("space-1", rebuilt.getId());
        Assertions.assertEquals("L-001", rebuilt.getLocalId());
        Assertions.assertEquals("Local Space Name", rebuilt.getLocalName());
        Assertions.assertNull(rebuilt.getClassCode());
        Assertions.assertNull(rebuilt.getObjType());
    }

    /**
     * Verifies multi-value building/floor ids keep only the first value.
     *
     * <p>Covers comma-separated strings and JSON arrays, including the single-element array;
     * the other field is judged independently.</p>
     *
     * @since v7.260802.3
     */
    @Test
    public void shouldKeepFirstValueOfMultiValueBuildingAndFloorIds() {
        CacheObjectRecordConverter converter = new CacheObjectRecordConverter(
                new CacheObjectExtensionProjector());
        Map<String, Object> actualFields = new LinkedHashMap<>();
        actualFields.put(DtpRwdObjectDTO.Fields.id, "equip-1");
        actualFields.put(DtpRwdObjectDTO.Fields.classCode,
                DtpRwdClassCodeEnum.SPACE.value());
        actualFields.put(DtpRwdObjectDTO.Fields.objType,
                DtpRwdObjectTypeEnum.SPACE.value());
        actualFields.put(DtpRwdEquipmentDTO.Fields.buildingId, "building-1, building-2");
        actualFields.put(DtpRwdEquipmentDTO.Fields.floorId, "floor-1");

        CacheObject cacheObject = converter.toCacheObject(actualFields);

        Assertions.assertEquals("building-1", cacheObject.getBuildingId());
        Assertions.assertEquals("floor-1", cacheObject.getFloorId());

        actualFields.put(DtpRwdEquipmentDTO.Fields.buildingId, "building-1");
        actualFields.put(DtpRwdEquipmentDTO.Fields.floorId, "[\"floor-1\",\"floor-2\"]");

        cacheObject = converter.toCacheObject(actualFields);

        Assertions.assertEquals("building-1", cacheObject.getBuildingId());
        Assertions.assertEquals("floor-1", cacheObject.getFloorId());

        actualFields.put(DtpRwdEquipmentDTO.Fields.floorId, "[\"floor-1\"]");

        cacheObject = converter.toCacheObject(actualFields);

        Assertions.assertEquals("floor-1", cacheObject.getFloorId());
    }

    /**
     * Verifies single-value building/floor ids are kept as-is.
     *
     * @since v7.260802.3
     */
    @Test
    public void shouldKeepSingleValueBuildingAndFloorIds() {
        CacheObjectRecordConverter converter = new CacheObjectRecordConverter(
                new CacheObjectExtensionProjector());
        Map<String, Object> actualFields = new LinkedHashMap<>();
        actualFields.put(DtpRwdObjectDTO.Fields.id, "equip-2");
        actualFields.put(DtpRwdObjectDTO.Fields.classCode,
                DtpRwdClassCodeEnum.SPACE.value());
        actualFields.put(DtpRwdObjectDTO.Fields.objType,
                DtpRwdObjectTypeEnum.SPACE.value());
        actualFields.put(DtpRwdEquipmentDTO.Fields.buildingId, "building-1");
        actualFields.put(DtpRwdEquipmentDTO.Fields.floorId, "floor-1");

        CacheObject cacheObject = converter.toCacheObject(actualFields);

        Assertions.assertEquals("building-1", cacheObject.getBuildingId());
        Assertions.assertEquals("floor-1", cacheObject.getFloorId());
    }

    /**
     * Verifies missing building/floor ids stay SQL NULL.
     *
     * @since v7.260802.3
     */
    @Test
    public void shouldKeepNullWhenBuildingAndFloorIdsMissing() {
        CacheObjectRecordConverter converter = new CacheObjectRecordConverter(
                new CacheObjectExtensionProjector());
        Map<String, Object> actualFields = new LinkedHashMap<>();
        actualFields.put(DtpRwdObjectDTO.Fields.id, "equip-3");
        actualFields.put(DtpRwdObjectDTO.Fields.classCode,
                DtpRwdClassCodeEnum.SPACE.value());
        actualFields.put(DtpRwdObjectDTO.Fields.objType,
                DtpRwdObjectTypeEnum.SPACE.value());

        CacheObject cacheObject = converter.toCacheObject(actualFields);

        Assertions.assertNull(cacheObject.getBuildingId());
        Assertions.assertNull(cacheObject.getFloorId());
    }
}
