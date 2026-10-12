package com.persagy.apccontrol.basic.model.base.domain;

/**
 * 聚合根
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public interface AggregateRoot<ID extends Identifier> extends Entity<ID> {

    /**
     * 获取标识符
     *
     * @return 标识符
     * @since 5.240602.0
     */
    ID getIdentifier();

}
