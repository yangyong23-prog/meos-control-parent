package com.persagy.apccontrol.task;

import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base XXL-JOB project dispatch tests.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class BaseXXJobTest {

    /**
     * Existing jobs keep their historical INFO traversal progress logs by default.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldKeepInfoTraversalProgressLogsByDefault() {
        assertEquals(Level.INFO, new BaseXXJob().traversalProgressLogLevel());
    }

    /**
     * Existing scheduled jobs must keep propagating synchronous dispatch failures.
     *
     * @throws Exception reflection failure
     * @since v7.260701.0
     */
    @Test
    public void shouldPropagateSynchronousFailureForExistingJobs() throws Exception {
        IllegalStateException failure = new IllegalStateException("dispatch failed");
        TecXxJobHandler handler = this.mockFailingHandler(failure);
        AtomicBoolean hasProjectFailed = new AtomicBoolean(false);

        try {
            this.invokeHandleProject(handler, false, hasProjectFailed);
            fail("Expected synchronous dispatch failure");
        } catch (InvocationTargetException exception) {
            assertSame(failure, exception.getCause());
        }
        assertFalse(hasProjectFailed.get());
    }

    /**
     * Initialization jobs aggregate synchronous project failures and continue.
     *
     * @throws Exception reflection failure
     * @since v7.260701.0
     */
    @Test
    public void shouldAggregateSynchronousFailureForInitializationJobs() throws Exception {
        TecXxJobHandler handler = this.mockFailingHandler(new IllegalStateException("dispatch failed"));
        AtomicBoolean hasProjectFailed = new AtomicBoolean(false);

        this.invokeHandleProject(handler, true, hasProjectFailed);

        assertTrue(hasProjectFailed.get());
    }

    private TecXxJobHandler mockFailingHandler(IllegalStateException failure) {
        TecXxJobHandler handler = mock(TecXxJobHandler.class);
        when(handler.processAsync(eq("group"), any(ProjectDTO.class))).thenThrow(failure);
        return handler;
    }

    private void invokeHandleProject(
            TecXxJobHandler handler,
            boolean failOnProjectFailure,
            AtomicBoolean hasProjectFailed) throws Exception {
        Method method = BaseXXJob.class.getDeclaredMethod(
                "handleProject",
                TecXxJobHandler.class,
                int.class,
                int.class,
                String.class,
                java.util.List.class,
                String.class,
                boolean.class,
                boolean.class,
                AtomicBoolean.class);
        method.setAccessible(true);
        ProjectDTO project = new ProjectDTO();
        project.setProjectId("project");
        method.invoke(
                new BaseXXJob(),
                handler,
                1,
                0,
                "group",
                Collections.singletonList(project),
                null,
                false,
                failOnProjectFailure,
                hasProjectFailed);
    }
}
