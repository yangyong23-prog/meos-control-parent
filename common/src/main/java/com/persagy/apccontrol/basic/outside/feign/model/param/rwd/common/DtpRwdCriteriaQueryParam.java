package com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectOrderParam;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * 物理世界标准查询参数
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtpRwdCriteriaQueryParam<T> {

    /**
     * 分页参数
     */
    @JsonUnwrapped
    private DtpRwdPageQuery page;

    /**
     * 排序参数
     */
    private Collection<DtpRwdObjectOrderParam> orderCustoms;

    /**
     * 标准查询参数
     */
    private T criteria;

    /**
     * 仅查询字段
     */
    private Collection<String> withColumns;

    /**
     * 是否只统计数量, 默认 [false]
     */
    protected Boolean onlyCount;

    /**
     * 构造器
     *
     * @param criteria 标准查询参数
     * @since v5.1.0
     */
    public DtpRwdCriteriaQueryParam(T criteria) {
        this.criteria = criteria;
    }

}
