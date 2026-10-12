package com.persagy.apccontrol.basic.model.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;

/**
 * 基础String类型输入ID DO
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BaseInputIdDO extends BaseDO implements Serializable {

    /**
     * 序列化版本号。
     *
     * @since v7.260701.0
     */
    private static final long serialVersionUID = -2423028294178440337L;

    /**
     * 调用方输入的字符串 ID。
     *
     * @since v7.260701.0
     */
    @TableId(value = "id", type = IdType.INPUT)
    protected String id;

    @Override
    public @NonNull BaseDO clearSaveParameter() {
        return super.clearSaveParameter();
    }
}
