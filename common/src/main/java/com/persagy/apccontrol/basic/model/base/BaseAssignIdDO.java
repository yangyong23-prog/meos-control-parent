package com.persagy.apccontrol.basic.model.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 基础Long类型ID DO
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BaseAssignIdDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = -6641259008967030660L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    protected Long id;

    @Override
    public BaseDO clearSaveParameter() {
        this.setId(null);
        return super.clearSaveParameter();
    }
}