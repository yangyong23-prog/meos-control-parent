package com.persagy.apccontrol.config;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.CommonConst;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;

/**
 * Swagger配置
 *
 * @author JiYinchuan
 * @since v5.0.0
 */
@SpringBootConfiguration
public class SwaggerConfiguration {

    private static final String PACKAGE_CONTROLLER = "com.persagy.apccontrol.";
    private static final String DEFAULT_HEADER_GROUP_CODE = CommonConst.GROUP_CODE;
    private static final String DEFAULT_GROUP_CODE = "TYIFC";

    @Bean
    public GroupedOpenApi allOpenApi() {
        return buildGroupedOpenApi(LocaleMessageUtil.getMessage("key_166cc72e891343d08decf2ffcbc3c155"), "controller");
    }

    /**
     * 构建分组接口文档
     *
     * @param groupName 分组名称
     * @param packages  接口路径
     * @return 分组接口文档
     * @since v5.0.0
     */
    private GroupedOpenApi buildGroupedOpenApi(@NonNull String groupName, @NonNull String packages) {
        return GroupedOpenApi.builder().group(groupName)
                .packagesToScan(PACKAGE_CONTROLLER + packages)
                .pathsToMatch("/**")
                .addOperationCustomizer((operation, handlerMethod) ->
                        operation.addParametersItem(new HeaderParameter()
                                        .required(true)
                                        .name(DEFAULT_HEADER_GROUP_CODE)
                                        .example(DEFAULT_GROUP_CODE)
                                        .description(LocaleMessageUtil.getMessage("key_1c326dbd5fef4ea9bcab016fc871ff86")))
                                .addParametersItem(new HeaderParameter()
                                        .required(true)
                                        .name("LOGIN-TYPE")
                                        .example("saas")
                                        .description("LOGIN-TYPE")))
                .build();
    }
}