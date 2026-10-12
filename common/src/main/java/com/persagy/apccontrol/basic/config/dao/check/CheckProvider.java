package com.persagy.apccontrol.basic.config.dao.check;

/**
 * 检查提供者
 *
 * <p>该类主要用于统一所有的检查Service, 便于进行集成使用
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface CheckProvider extends CheckOperationService, CheckExistsService, CheckIdempotencyService {
}
