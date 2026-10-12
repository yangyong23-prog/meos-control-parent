/*
package com.persagy.apccontrol.basic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.framework.rabbitmq.handler.RabbitMessageSender;
import com.rabbitmq.client.impl.CredentialsProvider;
import com.rabbitmq.client.impl.CredentialsRefreshService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.amqp.rabbit.config.AbstractRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.connection.SimplePropertyValueConnectionNameStrategy;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

*/
/**
 * RabbitMq配置
 *
 * @author JiYinchuan
 * @since v5.7.0
 *//*

@SpringBootConfiguration
@Slf4j
public class RabbitConfiguration implements InitializingBean {

    public static final String MESSAGE_CONVERTER_SIMPLE_BEAN_NAME = "simpleMessageConverter";
    private static final AtomicInteger COUNTER = new AtomicInteger();

    @Autowired
    @Qualifier("rabbitListenerContainerFactory")
    private AbstractRabbitListenerContainerFactory<?> rabbitListenerContainerFactory;

    @Bean
    @Lazy
    public RabbitMessageSender rabbitMessageSender() {
        return new RabbitMessageSender();
    }

    @Bean
    public SimplePropertyValueConnectionNameStrategy simplePropertyValueConnectionNameStrategy(
            @Value("${spring.application.name}") String applicationName) {
        return new SimplePropertyValueConnectionNameStrategy(applicationName
                + "#" + ObjectUtils.getIdentityHexString(this)
                + ":" + COUNTER.getAndIncrement());
    }

    @Bean
    @Primary
    public CachingConnectionFactory rabbitConnectionFactory(
            RabbitProperties properties,
            ResourceLoader resourceLoader, ObjectProvider<CredentialsProvider> credentialsProvider,
            ObjectProvider<CredentialsRefreshService> credentialsRefreshService,
            ObjectProvider<ConnectionNameStrategy> connectionNameStrategy) throws Exception {
        CachingConnectionFactory factory = new CachingConnectionFactory(
                Objects.requireNonNull(getRabbitConnectionFactoryBean(properties,
                        resourceLoader, credentialsProvider, credentialsRefreshService).getObject()));
        PropertyMapper map = PropertyMapper.get();
        map.from(properties::determineAddresses).to(factory::setAddresses);
        map.from(properties::getAddressShuffleMode).whenNonNull().to(factory::setAddressShuffleMode);
        map.from(properties::isPublisherReturns).to(factory::setPublisherReturns);
        map.from(properties::getPublisherConfirmType).whenNonNull().to(factory::setPublisherConfirmType);
        RabbitProperties.Cache.Channel channel = properties.getCache().getChannel();
        map.from(channel::getSize).whenNonNull().to(factory::setChannelCacheSize);
        map.from(channel::getCheckoutTimeout).whenNonNull().as(Duration::toMillis)
                .to(factory::setChannelCheckoutTimeout);
        RabbitProperties.Cache.Connection connection = properties.getCache().getConnection();
        map.from(connection::getMode).whenNonNull().to(factory::setCacheMode);
        map.from(connection::getSize).whenNonNull().to(factory::setConnectionCacheSize);
        map.from(connectionNameStrategy::getIfUnique).whenNonNull().to(factory::setConnectionNameStrategy);
        return factory;
    }

    @Bean
    @Primary
    public MessageConverter messageConverter(ObjectProvider<ObjectMapper> objectProvider) {
        return new Jackson2JsonMessageConverter(objectProvider.getIfUnique(ObjectMapper::new));
    }

    @Bean(MESSAGE_CONVERTER_SIMPLE_BEAN_NAME)
    public MessageConverter messageConverter() {
        return new SimpleMessageConverter();
    }

    @Override
    public void afterPropertiesSet() {
        rabbitListenerContainerFactory.setErrorHandler(t ->
                LogExecutor.instance().error(log, "RabbitErrorHandler", "消息异常", t, Arrays.asList(
                        Pair.of("errorType", t.getClass().getSimpleName() + ".class"),
                        Pair.of("errorMessage", t.getLocalizedMessage()),
                        Pair.of("errorCauseType", t.getCause().getClass().getSimpleName() + ".class"),
                        Pair.of("errorCauseMessage", t.getCause().getLocalizedMessage())
                )));
    }

    private RabbitConnectionFactoryBean getRabbitConnectionFactoryBean(
            RabbitProperties properties,
            ResourceLoader resourceLoader, ObjectProvider<CredentialsProvider> credentialsProvider,
            ObjectProvider<CredentialsRefreshService> credentialsRefreshService) {
        RabbitConnectionFactoryBean factory = new RabbitConnectionFactoryBean();
        factory.setResourceLoader(resourceLoader);
        PropertyMapper map = PropertyMapper.get();
        map.from(properties::determineHost).whenNonNull().to(factory::setHost);
        map.from(properties::determinePort).to(factory::setPort);
        map.from(properties::determineUsername).whenNonNull().to(factory::setUsername);
        map.from(properties::determinePassword).whenNonNull().to(factory::setPassword);
        map.from(properties::determineVirtualHost).whenNonNull().to(factory::setVirtualHost);
        map.from(properties::getRequestedHeartbeat).whenNonNull().asInt(Duration::getSeconds)
                .to(factory::setRequestedHeartbeat);
        map.from(properties::getRequestedChannelMax).to(factory::setRequestedChannelMax);
        RabbitProperties.Ssl ssl = properties.getSsl();
        if (ssl.determineEnabled()) {
            factory.setUseSSL(true);
            map.from(ssl::getAlgorithm).whenNonNull().to(factory::setSslAlgorithm);
            map.from(ssl::getKeyStoreType).to(factory::setKeyStoreType);
            map.from(ssl::getKeyStore).to(factory::setKeyStore);
            map.from(ssl::getKeyStorePassword).to(factory::setKeyStorePassphrase);
            map.from(ssl::getTrustStoreType).to(factory::setTrustStoreType);
            map.from(ssl::getTrustStore).to(factory::setTrustStore);
            map.from(ssl::getTrustStorePassword).to(factory::setTrustStorePassphrase);
            map.from(ssl::isValidateServerCertificate)
                    .to((validate) -> factory.setSkipServerCertificateValidation(!validate));
            map.from(ssl::getVerifyHostname).to(factory::setEnableHostnameVerification);
        }
        map.from(properties::getConnectionTimeout).whenNonNull().asInt(Duration::toMillis)
                .to(factory::setConnectionTimeout);
        map.from(properties::getChannelRpcTimeout).whenNonNull().asInt(Duration::toMillis)
                .to(factory::setChannelRpcTimeout);
        map.from(credentialsProvider::getIfUnique).whenNonNull().to(factory::setCredentialsProvider);
        map.from(credentialsRefreshService::getIfUnique).whenNonNull().to(factory::setCredentialsRefreshService);
        factory.afterPropertiesSet();
        return factory;
    }
}
*/
