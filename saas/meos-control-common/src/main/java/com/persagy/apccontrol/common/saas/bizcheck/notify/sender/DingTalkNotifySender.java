package com.persagy.apccontrol.common.saas.bizcheck.notify.sender;

import com.persagy.apccontrol.common.saas.bizcheck.notify.BizCheckNotifyProperties;
import com.persagy.api.IMessageApi;
import com.persagy.dto.DingTalkPersonalMessageSendReq;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 钉钉消息发送器
 *
 * @since 7.260601.0
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DingTalkNotifySender {

    private final IMessageApi messageApi;
    private final BizCheckNotifyProperties bizCheckNotifyProperties;

    /**
     * 向指定手机号列表发送钉钉消息
     *
     * @param phones         收件人手机号列表
     * @param messageContent 消息内容
     */
    public void sendAll(List<String> phones, String messageContent) {
        if (phones == null || phones.isEmpty()) {
            return;
        }
        try {
            String groupCode = ContextUtil.getGroupCode();
            String projectId = ContextUtil.getProjectId();

            DingTalkPersonalMessageSendReq req = new DingTalkPersonalMessageSendReq();
            req.setDingUserPhoneNumbers(phones);
            req.setContent(messageContent);
            req.setGroup(bizCheckNotifyProperties.getGroup());

            messageApi.sendDingTalkPersonalMessage(groupCode, projectId, req);
        } catch (Exception e) {
            log.warn("BizCheck通知：钉钉发送失败，不阻塞主流程. phones={}", phones, e);
        }
    }
}
