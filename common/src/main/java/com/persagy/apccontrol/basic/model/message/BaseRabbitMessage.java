package com.persagy.apccontrol.basic.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 基础 RabbitMQ 消息
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseRabbitMessage extends BaseMessage {

    public static final String EXCHANGE_PREFIX = "meos_control_";

    /**
     * 获取交换机
     *
     * @return 获取交换机
     * @since 5.240402.0
     */
    public String getExchange() {
        return EXCHANGE_PREFIX + exchange();
    }

    /**
     * 获取路由Key
     *
     * @return 路由Key
     * @since 5.240402.0
     */
    public String getRoutingKey() {
        return EXCHANGE_PREFIX + routingKey();
    }

    /**
     * 获取交换机
     *
     * @return 获取交换机
     * @since 5.240402.0
     */
    protected abstract String exchange();

    /**
     * 获取路由Key
     *
     * @return 路由Key
     * @since 5.240402.0
     */
    protected abstract String routingKey();

}
