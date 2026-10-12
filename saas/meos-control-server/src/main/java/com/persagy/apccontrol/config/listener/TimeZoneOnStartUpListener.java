package com.persagy.apccontrol.config.listener;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.param.common.CommonConfigSaveParam;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.cloud.timezone.TimeZoneManager;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;

/**
 * 自启动-项目时区设置
 *
 * @author JiYinchuan
 * @since 7.251001.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TimeZoneOnStartUpListener implements ApplicationListener<ApplicationStartedEvent>, LogComponent {

    private final CommonConfigService commonConfigService;

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        Map<String, ZoneId> projectIdAndTimeZone = this.readProjectIdAndTimeZone();
        if (MapUtils.isEmpty(projectIdAndTimeZone)) {
            return;
        }
        for (Map.Entry<String, ZoneId> entry : projectIdAndTimeZone.entrySet()) {
            String projectId = entry.getKey();
            ZoneId zoneId = entry.getValue();

            CommonConfigSaveParam addParam = new CommonConfigSaveParam();
            addParam.setTag(this.parseUniqueTag(projectId));
            addParam.setConfig(zoneId.getId());
            addParam.setRemark("项目时区配置");
            commonConfigService.saveConfig(addParam);
        }
        logInstance().info("加载项目时区配置成功", Pair.of("data", projectIdAndTimeZone));
    }

    /**
     * 解析项目唯一标签
     *
     * <p><b>Warning:</b> {@literal TIME_ZONE} 标识与 Mysql中函数 {@literal parse_project_timestamp} 对应, 不可随意修改</p>
     *
     * @param projectId 项目ID
     * @return 项目唯一标签
     * @since 7.251001.0
     */
    private String parseUniqueTag(@NonNull String projectId) {
        return "TIME_ZONE-" + projectId;
    }

    /**
     * 读取项目时区配置
     *
     * @return 项目时区配置
     * @since 7.251001.0
     */
    @SuppressWarnings("unchecked")
    private Map<String, ZoneId> readProjectIdAndTimeZone() {
        try {
            logInstance().trace("开始读取项目时区配置");
            Map<String, ZoneId> projectIdAndTimeZone = (Map<String, ZoneId>) FieldUtils.readStaticField(
                    TimeZoneManager.class, "projectTimeZoneMap", true);
            if (MapUtils.isEmpty(projectIdAndTimeZone)) {
                logInstance().info("项目时区配置为空");
                return Collections.emptyMap();
            }
            logInstance().trace("读取项目时区配置成功", Pair.of("data", projectIdAndTimeZone));
            return projectIdAndTimeZone;
        } catch (IllegalAccessException e) {
            logInstance().error("读取项目时区配置失败", e);
            throw new ApplicationException(ErrorCodeConstants.SYSTEM_ERROR_B0001, "读取项目时区配置失败");
        }
    }

    @Override
    public String logTag() {
        return "自启动-项目时区设置";
    }
}
