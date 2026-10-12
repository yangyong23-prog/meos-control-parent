package com.persagy.apccontrol.basic.model.base.domain;

import com.persagy.apccontrol.basic.executor.log.LogComponent;

import java.io.Serializable;

/**
 * 网关
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public interface Gateway<ID extends Serializable, T extends Entity<ID>> extends Infrastructure<ID, T>, LogComponent {

}
