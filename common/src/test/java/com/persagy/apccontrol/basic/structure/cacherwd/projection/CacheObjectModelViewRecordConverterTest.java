package com.persagy.apccontrol.basic.structure.cacherwd.projection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache object model view independent-column projection tests.
 *
 * @author JiYinchuan
 * @since v7.260802.3
 */
public class CacheObjectModelViewRecordConverterTest {

    /**
     * Project ObjectMapper holder used by the projection utilities.
     *
     * @since v7.260802.3
     */
    private Field objectMapperField;

    /**
     * ObjectMapper installed before this test.
     *
     * @since v7.260802.3
     */
    private Object originalObjectMapper;

    /**
     * Installs an isolated mapper for this pure unit test.
     *
     * @throws Exception when the shared mapper field cannot be accessed
     * @since v7.260802.3
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
     * @since v7.260802.3
     */
    @AfterEach
    public void tearDown() throws Exception {
        this.objectMapperField.set(null, this.originalObjectMapper);
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
        CacheObjectModelViewRecordConverter converter = new CacheObjectModelViewRecordConverter(
                new CacheObjectExtensionProjector());
        Map<String, Object> actualFields = new LinkedHashMap<>();
        actualFields.put(DtpRwdObjectDTO.Fields.id, "equip-1");
        actualFields.put(DtpRwdObjectDTO.Fields.classCode,
                DtpRwdClassCodeEnum.SPACE.value());
        actualFields.put(DtpRwdObjectDTO.Fields.objType,
                DtpRwdObjectTypeEnum.SPACE.value());
        actualFields.put(DtpRwdEquipmentDTO.Fields.buildingId, "building-1,building-2");
        actualFields.put(DtpRwdEquipmentDTO.Fields.floorId, "floor-1");

        CacheObjectModelView modelView = converter.toModelView(actualFields);

        Assertions.assertEquals("building-1", modelView.getBuildingId());
        Assertions.assertEquals("floor-1", modelView.getFloorId());

        actualFields.put(DtpRwdEquipmentDTO.Fields.buildingId, "building-1");
        actualFields.put(DtpRwdEquipmentDTO.Fields.floorId, "[\"floor-1\",\"floor-2\"]");

        modelView = converter.toModelView(actualFields);

        Assertions.assertEquals("building-1", modelView.getBuildingId());
        Assertions.assertEquals("floor-1", modelView.getFloorId());

        actualFields.put(DtpRwdEquipmentDTO.Fields.floorId, "[\"floor-1\"]");

        modelView = converter.toModelView(actualFields);

        Assertions.assertEquals("floor-1", modelView.getFloorId());
    }

    /**
     * Verifies single-value building/floor ids are kept as-is.
     *
     * @since v7.260802.3
     */
    @Test
    public void shouldKeepSingleValueBuildingAndFloorIds() {
        CacheObjectModelViewRecordConverter converter = new CacheObjectModelViewRecordConverter(
                new CacheObjectExtensionProjector());
        Map<String, Object> actualFields = new LinkedHashMap<>();
        actualFields.put(DtpRwdObjectDTO.Fields.id, "equip-2");
        actualFields.put(DtpRwdObjectDTO.Fields.classCode,
                DtpRwdClassCodeEnum.SPACE.value());
        actualFields.put(DtpRwdObjectDTO.Fields.objType,
                DtpRwdObjectTypeEnum.SPACE.value());
        actualFields.put(DtpRwdEquipmentDTO.Fields.buildingId, "building-1");
        actualFields.put(DtpRwdEquipmentDTO.Fields.floorId, "floor-1");

        CacheObjectModelView modelView = converter.toModelView(actualFields);

        Assertions.assertEquals("building-1", modelView.getBuildingId());
        Assertions.assertEquals("floor-1", modelView.getFloorId());
    }

    /**
     * Verifies missing building/floor ids stay SQL NULL.
     *
     * @since v7.260802.3
     */
    @Test
    public void shouldKeepNullWhenBuildingAndFloorIdsMissing() {
        CacheObjectModelViewRecordConverter converter = new CacheObjectModelViewRecordConverter(
                new CacheObjectExtensionProjector());
        Map<String, Object> actualFields = new LinkedHashMap<>();
        actualFields.put(DtpRwdObjectDTO.Fields.id, "equip-3");
        actualFields.put(DtpRwdObjectDTO.Fields.classCode,
                DtpRwdClassCodeEnum.SPACE.value());
        actualFields.put(DtpRwdObjectDTO.Fields.objType,
                DtpRwdObjectTypeEnum.SPACE.value());

        CacheObjectModelView modelView = converter.toModelView(actualFields);

        Assertions.assertNull(modelView.getBuildingId());
        Assertions.assertNull(modelView.getFloorId());
    }
}
