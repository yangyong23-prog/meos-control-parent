package com.persagy.apccontrol.base;

import com.persagy.apccontrol.ApcControlServerApplication;
import com.persagy.framework.common.context.ContextUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

/**
 * 基础测试类
 *
 * @author JiYinchuan
 * @since 24Q1 T3S2-5.18.0
 */
@Data
@SpringBootTest(classes = ApcControlServerApplication.class)
public abstract class BaseTest {

    /**
     * SpringMvc测试
     */
    protected static MockMvc mockMvc;

    /**
     * 集团编码
     */
    protected String groupCode;

    /**
     * 项目ID
     */
    protected String projectId;

    @BeforeAll
    static void setup(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).defaultResponseCharacterEncoding(StandardCharsets.UTF_8).build();
    }

    @BeforeEach
    void beforeEach(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).defaultResponseCharacterEncoding(StandardCharsets.UTF_8).build();
        setGroupCode(StringUtils.defaultIfBlank(getGroupCode(), defaultGroupCode()));
        if (StringUtils.isNotBlank(getGroupCode())) {
            ContextUtil.setTenant(getGroupCode());
            ContextUtil.setGroupCode(getGroupCode());
        }
        if (StringUtils.isNotBlank(getProjectId())) {
            ContextUtil.setProjectId(getProjectId());
        }
    }

    @AfterEach
    void afterEach() {
        ContextUtil.unloadContext();
    }

    /**
     * 集团编码
     *
     * @return 集团编码
     * @since v5.0.0
     */
    protected abstract String defaultGroupCode();

}
