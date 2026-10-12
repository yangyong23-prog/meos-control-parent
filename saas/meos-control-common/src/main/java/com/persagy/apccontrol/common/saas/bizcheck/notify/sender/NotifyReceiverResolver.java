package com.persagy.apccontrol.common.saas.bizcheck.notify.sender;

import com.persagy.apccontrol.common.saas.bizcheck.notify.BizCheckNotifyProperties;
import com.persagy.org.api.dto.PositionNamesQuery;
import com.persagy.org.api.feign.GroupUserClient;
import com.persagy.org.api.vo.UserBasicInfoVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 通知收件人解析器：实时同步查询岗位手机号
 *
 * @since 7.260601.0
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NotifyReceiverResolver {

    private final GroupUserClient groupUserClient;
    private final BizCheckNotifyProperties properties;

    /**
     * 解析收件人手机号
     *
     * @param projectId 项目ID
     * @return 手机号列表
     */
    public List<String> resolvePhoneNumbers(String projectId) {
        try {
            PositionNamesQuery query = new PositionNamesQuery();
            query.setPositionNames(properties.getPositionNames());
            query.setProjectId(projectId);
            List<UserBasicInfoVo> users = groupUserClient.queryByPositionNamesAndProjectId(query);
            List<String> phones = users == null ? Collections.emptyList() :
                    users.stream()
                            .map(UserBasicInfoVo::getMobileNum)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());
            if (phones.isEmpty()) {
                log.warn("BizCheck通知：未查询到收件人. positionNames={}, projectId={}",
                        properties.getPositionNames(), projectId);
                return Collections.emptyList();
            }
            return phones;
        } catch (Exception e) {
            log.error("BizCheck通知：查询收件人手机号失败. projectId={}", projectId, e);
            throw e;
        }
    }
}
