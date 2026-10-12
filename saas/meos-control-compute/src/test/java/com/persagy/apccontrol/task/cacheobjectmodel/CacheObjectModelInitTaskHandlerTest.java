package com.persagy.apccontrol.task.cacheobjectmodel;

import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.LinkedHashSet;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cache object model initialization task logging tests.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheObjectModelInitTaskHandlerTest {

    /**
     * Routine traversal logs stay out of XXL-JOB while one successful project terminal log remains.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldKeepOnlyOneProjectTerminalInfoLog() {
        CacheRwdDomainService domainService = mock(CacheRwdDomainService.class);
        CacheObjectModelStatusService statusService = mock(CacheObjectModelStatusService.class);
        Logger logger = mock(Logger.class);
        LinkedHashSet<RunningMonitorTypeEnum> supportedTypes = new LinkedHashSet<>(Arrays.asList(
                RunningMonitorTypeEnum.AIR_TERMINAL,
                RunningMonitorTypeEnum.LIGHTING));
        when(statusService.supportedRunningMonitorTypes()).thenReturn(supportedTypes);
        when(domainService.buildCache(RunningMonitorTypeEnum.AIR_TERMINAL)).thenReturn(true);
        when(domainService.buildCache(RunningMonitorTypeEnum.LIGHTING)).thenReturn(true);
        TestCacheObjectModelInitTaskHandler handler = new TestCacheObjectModelInitTaskHandler(
                domainService, statusService, logger);
        ProjectDTO project = new ProjectDTO();
        project.setProjectId("project-1");

        handler.process("group-1", project);

        Assertions.assertEquals(Level.DEBUG, handler.traversalProgressLogLevel());
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).info(messageCaptor.capture(), isNull(Throwable.class));
        String message = messageCaptor.getValue();
        Assertions.assertTrue(message.contains("缓存对象模型项目构建成功"));
        Assertions.assertTrue(message.contains("runningMonitorTypes: [AIR_TERMINAL, LIGHTING]"));
        Assertions.assertTrue(message.contains("costTimeMs:"));
        Assertions.assertEquals(1, StringUtils.countMatches(message, "groupCode: group-1"));
        Assertions.assertEquals(1, StringUtils.countMatches(message, "projectId: project-1"));
        verify(domainService, times(1)).buildCache(RunningMonitorTypeEnum.AIR_TERMINAL);
        verify(domainService, times(1)).buildCache(RunningMonitorTypeEnum.LIGHTING);
    }

    /**
     * Handler with an injectable logger for isolated log assertions.
     *
     * @since v7.260701.0
     */
    private static class TestCacheObjectModelInitTaskHandler extends CacheObjectModelInitTaskHandler {

        /**
         * Captured logger.
         *
         * @since v7.260701.0
         */
        private final Logger logger;

        /**
         * Create a test handler.
         *
         * @param domainService cache domain service
         * @param statusService cache status service
         * @param logger captured logger
         * @since v7.260701.0
         */
        private TestCacheObjectModelInitTaskHandler(
                CacheRwdDomainService domainService,
                CacheObjectModelStatusService statusService,
                Logger logger) {
            super(domainService, statusService);
            this.logger = logger;
        }

        @Override
        public Logger log() {
            return this.logger;
        }
    }
}
