package com.persagy.apccontrol.task;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.service.dangerevent.impl.DangerEventServiceImpl;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * @description:空间分析
 * @author: wxf
 * @create: 2024-11-29 18:01
 **/
@Component("pullDangerEventTaskHandler")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PullDangerEventTaskHandler implements TecXxJobHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PullDangerEventTaskHandler.class);
    private static final String LOG_TAG = "风险事件-拉取数据";

    private final DangerEventServiceImpl dangerEventService;
    private final MidPermissionClientService midPermissionClientService;

    @Override
    public String getHandlerName() {
        return LocaleMessageUtil.getMessage("key_06abaee4de914771b23bc2cd2fba993b");
    }

    @Override
    public Future<String> processAsync(String groupCode, ProjectDTO projectDTO) {
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        try {
            dangerEventService.pullAllDangerEvent();
        } catch (Exception e) {
        } finally {
            ContextUtil.unloadContext();
        }

        return new AsyncResult<>(this.getHandlerName() + LocaleMessageUtil.getMessage("key_2fa808b2f7ea4112a5ed1750c743f957") + groupCode + ";projectId" + projectDTO.getProjectId());
    }

    @Override
    public void process(String groupCode, ProjectDTO projectDTO) {
    }

}