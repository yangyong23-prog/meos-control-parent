package com.persagy.apccontrol.basic.service.demand.blacklist.handler;


import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandBlacklistTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * 需求黑名单处理接口
 * 所有实现了该接口的类都会被执行
 * 需要判断该类
 */
public interface DemandBlacklistStrategy {


    /**
     * 处理需求黑名单
     * 返回黑名单空间列表
     *
     * @param jsonNode  配置参数
     * @return 黑名单空间列表
     */
    Set<String> execute(@NonNull DemandTemplateTypeEnum demandTemplateType,
                        DemandBlacklistTypeEnum type,
                        @NonNull JsonNode jsonNode);




}
