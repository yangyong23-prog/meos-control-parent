package com.persagy.apccontrol.basic.structure.demand.enumeration;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.dto.demand.blacklist.*;
import com.persagy.apccontrol.basic.service.demand.blacklist.handler.*;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 需求黑名单类型
 */
@Getter
@AllArgsConstructor
public enum DemandBlacklistTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * iot规则
     */
    INSTANTIATE_RULE("INSTANTIATE_RULE", "key_9d130df99c13459c9d5e576c88586582", DemandBlacklistInstantiateRuleStrategy.BEAN_NAME, DemandBlacklistInstantiateRuleDTO.class),

    /**
     * 所在楼层
     */
    IN_FLOOR("IN_FLOOR", "key_90ff2a4589a14144b361ebc22e06a20a", DemandBlacklistInFloorStrategy.BEAN_NAME, DemandBlacklistInFloorRuleDTO.class),

    /**
     * 空间功能类型
     */
    ROOM_FUNC_TYPE("ROOM_FUNC_TYPE", "key_c8e9a828419e4e2f81da655dd97ee277", DemandBlacklistRoomFuncTypeStrategy.BEAN_NAME, DemandBlacklistRoomFuncTypeRuleDTO.class),

    /**
     * 纳入空间
     */
    INTO_SPACES("INTO_SPACES", "key_165e93fa62ed4503bedc82731184e3be", "", DemandBlacklistIntoSpacesRuleDTO.class),

    /**
     * 移出空间
     */
    REMOVE_SPACES("REMOVE_SPACES", "key_3e2c213f8c42491b8b6dbe6c49c3fee4", "", DemandBlacklistRemoveSpacesRuleDTO.class),
    /**
     * 空调末端服务的空间
     */
    AIR_TERMINAL_SERVICE_SPACE("AIR_TERMINAL_SERVICE_SPACE", "key_047a66e4749b4823a39b63713599be73", DemandBlacklistAirTerminalStrategy.BEAN_NAME,
            DemandBlacklistAirTerminalServiceSpaceRuleDTO.class),
    /**
     * 系统服务的空间
     */
    SYSTEM_SERVICE_SPACE("SYSTEM_SERVICE_SPACE", "key_04b8fd38fa454234a8ace551043c28de", DemandBlacklistSystemServiceSpaceStrategy.BEAN_NAME,
            DemandBlacklistSystemServiceSpaceRuleDTO.class),
    
    
    
    ;

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;


    /**
     * 策略Bean名称
     */
    private final String beanName;

    private final Class<? extends BasicsDemandBlacklistRuleDTO> clazz;

    /**
     * 通过需求类型
     * 获取需求策略
     */
    public DemandBlacklistStrategy demandBlacklistHandler() {
        if (StringUtils.isBlank(getBeanName())) {
            return null;
        }
        return SpringUtil.getBean(getBeanName());
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DemandBlacklistTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandBlacklistTypeEnum.class);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }
}
