package com.persagy.apccontrol.basic.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.persagy.apccontrol.basic.config.dao.partition.DynamicPartitionTableNameInnerInterceptor;
import com.persagy.framework.database.datasource.BaseMybatisConfiguration;
import com.persagy.framework.database.properties.DatabaseProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 配置一些拦截器
 * 多租户必须要配置 不然不生效
 *
 * @author
 * @date 2021-11-18 0:34
 */
@Configuration
@EnableConfigurationProperties({DatabaseProperties.class})
public class PaasMybatisAutoConfiguration extends BaseMybatisConfiguration {

    @Autowired
    private DynamicPartitionTableNameInnerInterceptor dynamicPartitionTableNameInnerInterceptor;

    public PaasMybatisAutoConfiguration(DatabaseProperties databaseProperties) {
        super(databaseProperties);
    }

    @Override
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = super.mybatisPlusInterceptor();
        interceptor.addInnerInterceptor(dynamicPartitionTableNameInnerInterceptor);
        return interceptor;
    }
}
