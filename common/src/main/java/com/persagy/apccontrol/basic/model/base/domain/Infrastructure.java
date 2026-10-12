package com.persagy.apccontrol.basic.model.base.domain;

import com.persagy.apccontrol.basic.executor.log.LogComponent;

import java.io.Serializable;

/**
 * 基础设施
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
public interface Infrastructure<ID extends Serializable, T extends Entity<ID>> extends LogComponent {

}
