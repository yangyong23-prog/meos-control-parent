package com.persagy.apccontrol.basic.model.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * 输入型字符串主键基础实体测试
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class BaseInputIdDOTest {

    /**
     * 验证清理保存参数时保留调用方输入的字符串主键并清空四个审计字段。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldPreserveInputIdAndClearAuditFields() {
        BaseInputIdDO entity = new BaseInputIdDO();
        entity.setId("object-id");
        entity.setCreateTime(LocalDateTime.of(2026, 7, 14, 10, 0));
        entity.setCreateUser("creator");
        entity.setUpdateTime(LocalDateTime.of(2026, 7, 14, 11, 0));
        entity.setUpdateUser("updater");

        BaseDO result = entity.clearSaveParameter();

        Assertions.assertSame(entity, result);
        Assertions.assertEquals("object-id", entity.getId());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getCreateUser());
        Assertions.assertNull(entity.getUpdateTime());
        Assertions.assertNull(entity.getUpdateUser());
    }
}
