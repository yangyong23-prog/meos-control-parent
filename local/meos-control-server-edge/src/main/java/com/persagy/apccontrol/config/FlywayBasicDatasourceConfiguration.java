package com.persagy.apccontrol.config;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.output.ValidateOutput;
import org.flywaydb.core.api.output.ValidateResult;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Flyway基础数据库配置
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FlywayBasicDatasourceConfiguration implements InitializingBean, LogComponent {

    private static final String LOG_TAG = "Flyway";
    private static final String BASIC_DATASOURCE_NAME = "meos_control";

    private final Flyway flyway;

    @Override
    public void afterPropertiesSet() {
        ClassicConfiguration configuration = (ClassicConfiguration) flyway.getConfiguration();
        configuration.setLocations(new Location("classpath:db/flyway/meos_control_common"));
        configuration.setDefaultSchema(BASIC_DATASOURCE_NAME);
        configuration.setSchemas(BASIC_DATASOURCE_NAME);
        // 为了保证执行手动验证文件逻辑和自动验证文件逻辑一致, 强制指定为[true]
        configuration.setIgnorePendingMigrations(true);
        // 避免重复验证, 强制指定为[false]
        configuration.setValidateOnMigrate(false);
        Flyway newFlyway = Flyway.configure().configuration(new ClassicConfiguration(configuration)).load();
        ValidateResult validateResult = newFlyway.validateWithResult();
        if (!validateResult.validationSuccessful && !configuration.isCleanOnValidationError()) {
            if (CollectionUtils.isNotEmpty(validateResult.invalidMigrations)) {
                for (ValidateOutput invalidMigration : validateResult.invalidMigrations) {
                    LogExecutor.instance().error(log, LOG_TAG, "文件验证失败", Arrays.asList(
                            Pair.of("version", StringUtils.defaultIfBlank(invalidMigration.version, null)),
                            Pair.of("description", invalidMigration.description),
                            Pair.of("filepath", invalidMigration.filepath)
                    ));
                }
            }
            throw new FlywayValidateException(validateResult.errorDetails);
        }
        MigrateResult migrateResult = newFlyway.migrate();
        logInstance().info("Flyway执行完毕", Pair.of("migrateResult", JSONObject.toJSONString(migrateResult)));
    }

    @Override
    public String logTag() {
        return "Flyway-基础库";
    }
}
