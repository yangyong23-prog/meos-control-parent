package com.persagy.apccontrol.basic.config.dao.partition;


import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;

import java.lang.annotation.*;

/**
 * 分表注解
 *
 * <p>标记在实体类上
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@ConditionalOnWebApplication
public @interface PartitionTable {

}
