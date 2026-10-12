package com.persagy.apccontrol.basic.outside.feign.model.param.rwd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdPageQuery;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * 关系查询参数
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor(force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtpRwdRelationQueryParam {

    /**
     * 分页参数
     */
    @JsonUnwrapped
    private DtpRwdPageQuery page = new DtpRwdPageQuery();

    /**
     * 图类型编码
     */
    private String graphCode;

    /**
     * 边类型编码
     */
    private String relCode;

    /**
     * 主对象的筛选条件
     */
    private DtpRwdObjectQueryParam fromCriteria;

    /**
     * 从对象的筛选条件
     */
    private DtpRwdObjectQueryParam toCriteria;

    /**
     * 筛选条件
     */
    private DtpRwdObjectRelationQueryParam criteria;

    /**
     * 是否仅查询数量
     */
    private Boolean onlyCount;

    /**
     * column字段需为"prefix+信息点大驼峰式".prefix取值有"from","to","rel"
     */
    private String orders;

    /**
     * 构造器
     *
     * @param relationType 实例关联类型
     * @param fromCriteria 主对象的筛选条件
     * @param toCriteria   从对象的筛选条件
     * @since v5.1.0
     */
    public DtpRwdRelationQueryParam(
            @NonNull DtpRwdRelationTypeEnum relationType,
            @Nullable DtpRwdObjectQueryParam fromCriteria,
            @Nullable DtpRwdObjectQueryParam toCriteria) {
        this.graphCode = relationType.value().getLeft();
        this.relCode = relationType.value().getRight();
        this.fromCriteria = fromCriteria;
        this.toCriteria = toCriteria;
    }

    /**
     * 设置实例关联类型
     *
     * @param relationType 实例关联类型
     * @return 当前实例
     * @since v5.1.0
     */
    public void fillRelationType(@Nullable DtpRwdRelationTypeEnum relationType) {
        if (Objects.isNull(relationType)) {
            return;
        }
        this.graphCode = relationType.value().getLeft();
        this.relCode = relationType.value().getRight();
    }
}
