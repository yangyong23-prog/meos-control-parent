package com.persagy.apccontrol.basic.outside.feign.model.param.rwd;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.NonNull;

import java.util.Collection;

/**
 * 对象关联主对象查询参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@FieldNameConstants
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtpRwdObjectRelationQueryParam {

    /**
     * 实例关联类型
     */
    @NonNull
    @JsonIgnore
    private DtpRwdRelationTypeEnum relationType;

    /**
     * 图类型编码
     */
    @NonNull
    private String graphCode;

    /**
     * 边类型编码
     */
    @NonNull
    private String relCode;

    /**
     * 关联主对象ID
     */
    private Collection<String> objFrom;

    /**
     * 关联从对象ID
     */
    private Collection<String> objTo;

    private String relValue;

    /**
     * 构造器
     *
     * @since v5.8.0
     */
    public DtpRwdObjectRelationQueryParam(@NonNull DtpRwdRelationTypeEnum relationType) {
        this.relationType = relationType;
        this.graphCode = relationType.value().getLeft();
        this.relCode = relationType.value().getRight();
    }

    /**
     * 构造器
     *
     * @param graphCode 图类型编码
     * @param relCode   关联主对象ID
     * @since 5.240702.0
     */
    public DtpRwdObjectRelationQueryParam(@NonNull String graphCode, @NonNull String relCode) {
        this.graphCode = graphCode;
        this.relCode = relCode;
    }

    /**
     * 无参构造器
     *
     * @since v5.240602.0
     */
    public DtpRwdObjectRelationQueryParam() {
    }

    /**
     * 设置关联主对象ID
     *
     * @param objFrom 关联主对象ID
     * @return 当前实例
     * @since v5.8.0
     */
    public DtpRwdObjectRelationQueryParam setObjFrom(String objFrom) {
        this.objFrom = Lists.newArrayList(objFrom);
        return this;
    }

    /**
     * 设置关联主对象ID
     *
     * @param objFrom 关联主对象ID
     * @return 当前实例
     * @since v5.8.0
     */
    public DtpRwdObjectRelationQueryParam setObjFrom(Collection<String> objFrom) {
        this.objFrom = objFrom;
        return this;
    }

    /**
     * 设置关联主对象ID
     *
     * @param object 集团编码
     * @return 当前实例
     * @since 5.240801.0
     */
    @JsonSetter(Fields.objFrom)
    @SuppressWarnings("unchecked")
    public DtpRwdObjectRelationQueryParam setObjFrom(Object object) {
        if (object instanceof Collection) {
            return setObjFrom((Collection<String>) object);
        } else if (object instanceof String) {
            return setObjFrom((String) object);
        } else {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_57019827c2964f498d2b39ed550c81ee"));
        }
    }

    /**
     * 设置关联从对象ID
     *
     * @param objTo 关联从对象ID
     * @return 当前实例
     * @since v5.8.0
     */
    public DtpRwdObjectRelationQueryParam setObjTo(String objTo) {
        this.objTo = Lists.newArrayList(objTo);
        return this;
    }

    /**
     * 设置关联从对象ID
     *
     * @param objTo 关联从对象ID
     * @return 当前实例
     * @since v5.8.0
     */
    public DtpRwdObjectRelationQueryParam setObjTo(Collection<String> objTo) {
        this.objTo = objTo;
        return this;
    }

    /**
     * 设置关联主对象ID
     *
     * @param object 集团编码
     * @return 当前实例
     * @since 5.240801.0
     */
    @JsonSetter(Fields.objTo)
    @SuppressWarnings("unchecked")
    public DtpRwdObjectRelationQueryParam setObjTo(Object object) {
        if (object instanceof Collection) {
            return setObjTo((Collection<String>) object);
        } else if (object instanceof String) {
            return setObjTo((String) object);
        } else {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_57019827c2964f498d2b39ed550c81ee"));
        }
    }
}
