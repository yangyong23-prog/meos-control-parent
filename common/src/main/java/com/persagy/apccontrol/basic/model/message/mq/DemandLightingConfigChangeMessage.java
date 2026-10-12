package com.persagy.apccontrol.basic.model.message.mq;

import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingConfigChangeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.message.BaseRabbitMessage;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * 照明需求变化消息
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemandLightingConfigChangeMessage extends BaseRabbitMessage {

    public static final String EXCHANGE = "lighting_config_changed_exchange";

    /**
     * 配置变动类型消息
     */
    private List<Content> contents;


    /**
     * 从照明配置结果创建实例
     *
     * @param projectId 项目ID
     * @param contents  消息
     * @return 当前实例
     * @since 5.240402.0
     */
    public static DemandLightingConfigChangeMessage from(
            @NonNull String projectId,
            @NonNull List<Content> contents) {
        if (CollectionUtils.isEmpty(contents)) {
            return null;
        }
        DemandLightingConfigChangeMessage result = new DemandLightingConfigChangeMessage();
        result.setProjectId(projectId);
        result.setContents(contents);
        return result;
    }

    /**
     * 照明需求变化类型消息
     *
     * @author JiYinchuan
     * @since 5.240402.0
     */
    @Data
    @Accessors(chain = true)
    public static class Content {

        /**
         * 数据类型
         */
        private DataTypeEnum dataType;

        /**
         * 配置变动类型
         */
        private LightingConfigChangeTypeEnum configChangeType;

        /**
         * 变动模版
         */
        private DemandTemplateVO templateByChanged;

        /**
         * 从照明配置结果创建实例
         *
         * @param dataType         数据类型
         * @param configChangeType 配置变动类型
         * @param source           变动模版ID
         * @return 当前实例
         * @since 5.240402.0
         */
        public static Content from(
                @NonNull DataTypeEnum dataType,
                @NonNull LightingConfigChangeTypeEnum configChangeType,
                @Nullable DemandTemplateVO source) {
            if (Objects.isNull(source)) {
                return null;
            }
            Content result = new Content();
            result.setDataType(dataType);
            result.setConfigChangeType(configChangeType);
            result.setTemplateByChanged(source);
            return result;
        }
    }

    @Override
    public String exchange() {
        return EXCHANGE;
    }

    @Override
    public String routingKey() {
        return getProjectId();
    }
}
