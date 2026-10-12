package com.persagy.apccontrol.config.listener;

import com.persagy.apccontrol.basic.config.properties.saas.children.demand.DemandTaskOnStartedProperties;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.task.BaseTask;
import com.persagy.apccontrol.common.service.demand.DemandCalendarService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 启动时执行任务
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Component
@Slf4j
public class ComputeOnStartUpListener extends BaseTask implements ApplicationListener<ApplicationStartedEvent> {

    private static final String LOG_TAG = "自启动监听器";

    @Autowired
    private DemandTaskOnStartedProperties demandTaskOnStartedProperties;
    @Autowired
    private DemandCalendarService demandCalendarService;

    @Override
    @SneakyThrows
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        boolean initTemplateOnStartUp = demandTaskOnStartedProperties.isInitTemplateOnStartUp();
        if (!initTemplateOnStartUp) {
            return;
        }
        handlerProject(null, null, project -> {
            String projectId = project.getProjectId();
            // 初始化需求模版
            initTemplate(projectId);
        });
    }

    /**
     * 初始化模版
     *
     * @param projectId 项目ID
     * @since v6.0.0
     */
    private void initTemplate(@NonNull String projectId) {
        try {
            Map<DemandTemplateTypeEnum, List<String>> templateTypeAndTags =
                    demandCalendarService.initTemplate(projectId);
            if (MapUtils.isEmpty(templateTypeAndTags)) {
                LogExecutor.project(projectId).info(log, LOG_TAG, "所有需求模版均已初始化");
            } else {
                LogExecutor.project(projectId).info(log, LOG_TAG, "初始化需求模版成功",
                        Collections.singletonList(Pair.of("templateTypeAndTags", templateTypeAndTags)));
            }
        } catch (Exception e) {
            LogExecutor.project(projectId).error(log, LOG_TAG, "初始化需求模版失败", e);
        }
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
