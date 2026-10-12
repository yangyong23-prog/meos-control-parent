package com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject;

import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectModel;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * 信息点设备类过滤条件值对象
 *
 * @author JiYinchuan
 * @since 5.240802.0
 */
@Data
public class InfoCodeFilterValueObject {

    /**
     * 唯一标识
     */
    private String uniqueKey;

    /**
     * 自定义模版名称
     */
    private String customName;

    /**
     * 过滤IOT信息点规则
     */
    private Set<String> filterIotInfoCodeRules;

    /**
     * 白名单信息点名称, 为空时将不拼接任何信息点
     */
    private Set<String> showInfoCodesWhitelistInName;

    /**
     * 查询参数
     */
    private @Nullable DtpRwdObjectQueryParam queryParam;

    /**
     * 占位符数据源
     */
    private LinkedHashMap<String, InfoCodeTakeValueLogicRwdObjectModel> source;

}
