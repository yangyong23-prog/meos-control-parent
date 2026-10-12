package com.persagy.apccontrol.basic.model.base;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import lombok.Data;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 基础DO
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
public abstract class BaseDO implements Serializable {

    private static final long serialVersionUID = -418127014883081830L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_CREATE_TIME = "createTime";
    public static final String FIELD_CREATE_USER = "createUser";
    public static final String FIELD_UPDATE_TIME = "updateTime";
    public static final String FIELD_UPDATE_USER = "updateUser";
    public static final String FIELD_DELETED = "deleted";

    /**
     * 创建时间
     *
     * <p>新增时将自动填充当前时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    protected LocalDateTime createTime;

    /**
     * 创建人
     *
     * <p>新增时将自动填充当前上下文用户
     *
     * @see ContextUtil#getUserId()
     */
    @TableField(value = "create_user", fill = FieldFill.INSERT)
    protected String createUser;

    /**
     * 最后修改时间
     *
     * <p>新增和修改时自动填充当前时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime updateTime;

    /**
     * 最后修改时间
     *
     * <p>新增和修改时自动填充当前上下文用户
     *
     * @see ContextUtil#getUserId()
     */
    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    protected String updateUser;

    /**
     * 清空保存参数
     *
     * @return 当前实例
     * @since v5.6.0
     */
    public BaseDO clearSaveParameter() {
        this.setCreateTime(null);
        this.setCreateUser(null);
        return clearUpdateParameter();
    }

    /**
     * 清空更新参数
     *
     * @return 当前实例
     * @since v5.6.0
     */
    public BaseDO clearUpdateParameter() {
        this.setUpdateTime(null);
        this.setUpdateUser(null);
        return this;
    }

    /**
     * 填充 UPSERT 所需的审计字段。
     *
     * <p>手写 XML 不触发 MyBatis-Plus 自动填充。整个批次只读取一次当前用户和时间；创建审计
     * 字段只补缺失值，重复主键更新时保留数据库中的原创建信息；更新审计字段每次刷新为本批次
     * 统一时间和用户。</p>
     *
     * @param entities 待写入实体集合
     * @since v7.260701.0
     */
    public static void fillUpsertDefaultValue(
            @NonNull Collection<? extends BaseDO> entities) {
        String userId = ContextUtil.getUserId();
        LocalDateTime now = DateUtils.now();
        for (BaseDO entity : entities) {
            if (entity.getCreateTime() == null) {
                entity.setCreateTime(now);
            }
            if (entity.getCreateUser() == null) {
                entity.setCreateUser(userId);
            }
            entity.setUpdateTime(now);
            entity.setUpdateUser(userId);
        }
    }

    /**
     * 检查新增实体
     *
     * @since 5.240602.0
     */
    public void checkAddEntity() {
        Supplier<Boolean> checkedNullSupplier = checkAddEntitySupplier();
        if (Objects.nonNull(checkedNullSupplier) && BooleanUtils.isTrue(checkedNullSupplier.get())) {
            throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_479715bc39214b8abe6d18f9f0b313bd"));
        }
    }

    /**
     * 检查删除实体
     *
     * @since 5.240602.0
     */
    public void checkDelEntity() {
        Supplier<Boolean> checkedNullSupplier = checkDelEntitySupplier();
        if (Objects.nonNull(checkedNullSupplier) && BooleanUtils.isTrue(checkedNullSupplier.get())) {
            throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_aadd6bbd8b564b9988b2c131e93faca8"));
        }
    }

    /**
     * 检查新增实体Supplier
     *
     * @return 检查实体Supplier
     * @since 5.240602.0
     */
    protected Supplier<Boolean> checkAddEntitySupplier() {
        return null;
    }

    /**
     * 检查删除实体Supplier
     *
     * @return 检查实体Supplier
     * @since 5.240602.0
     */
    protected Supplier<Boolean> checkDelEntitySupplier() {
        return null;
    }
}
