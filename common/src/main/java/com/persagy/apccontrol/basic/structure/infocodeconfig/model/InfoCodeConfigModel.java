package com.persagy.apccontrol.basic.structure.infocodeconfig.model;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.BaseInfoCodeTackValueLogicModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTackLogicDemandTargetModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTackLogicIotPersistenceModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.StaticInfoCodeTackValueLogicModel;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * 信息点配置模型
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeConfigModel implements ValueObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoCodeConfigModel.class);
    private static final String LOG_TAG = "信息点-配置模型";
    private static final long serialVersionUID = -1199510598925682546L;

    // 机房空间功能类型列表配置
    public static final String COMPUTER_ROOM_SECURITY_ROOM_FUNC_TYPE = "computer_room_security_room_func_type";

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 信息点编码
     */
    private String infoCode;

    /**
     * 信息点名称
     */
    private String infoCodeName;

    /**
     * 信息点别名
     */
    @Nullable
    private String infoCodeAliasName;

    /**
     * 信息点类型
     */
    private InfoCodeTypeEnum infoCodeType;

    /**
     * 是否存在IOT规则, 仅为IOT信息点类型时存在
     *
     * @see InfoCodeTypeEnum#isIotInfoCode()
     */
    private Boolean existsIotRule;

    /**
     * 取值逻辑
     */
    @Nullable
    private JsonNode tackValueLogic;

    /**
     * 展示样式
     */
    @Nullable
    private String showStyle;

    /**
     * 信息点顺序
     */
    private Integer sequence;

    /**
     * 获取唯一ID
     *
     * @return 唯一ID
     * @since 7.250901.0
     */
    @JsonIgnore
    public String getUniqueKey(@NonNull String groupCode) {
        return new StringJoiner("-")
                .add(groupCode)
                .add(this.infoCodeType.value())
                .add(this.infoCode)
                .toString();
    }

    /**
     * 解析取值逻辑模型
     *
     * @return 取值逻辑模型
     * @since 5.241101.0
     */
    @SneakyThrows
    public StaticInfoCodeTackValueLogicModel analyzeTackValueLogicModel() {
        if (this.infoCodeType == InfoCodeTypeEnum.JSON_OBJECT) {
            StaticInfoCodeTackValueLogicModel model = new StaticInfoCodeTackValueLogicModel();
            if (Objects.nonNull(this.tackValueLogic)) {
                JsonNode execute = this.tackValueLogic.get(BaseInfoCodeTackValueLogicModel.Fields.execute);
                JsonNode filterProjectIds = this.tackValueLogic.get(BaseInfoCodeTackValueLogicModel.Fields.filterProjectIds);
                if (Objects.nonNull(execute) && execute.isBoolean()) {
                    model.setExecute(execute.asBoolean());
                }
                if (Objects.nonNull(filterProjectIds) && filterProjectIds.isArray()) {
                    Set<String> finalFilterProjectIds = ObjectMapperUtils.convertValue(filterProjectIds,
                            new TypeReference<Set<String>>() {
                            });
                    if (CollectionUtils.isNotEmpty(finalFilterProjectIds)) {
                        model.setFilterProjectIds(finalFilterProjectIds);
                    }
                }
            }
            return model;
        }
        return ObjectUtils.defaultIfNull(
                analyzeTackValueLogicModel(StaticInfoCodeTackValueLogicModel.class),
                StaticInfoCodeTackValueLogicModel.INSTANCE);
    }

    /**
     * 解析取值逻辑模型
     *
     * @param cls 取值逻辑类型
     * @param <T> 取值逻辑模型类型
     * @return 取值逻辑模型
     * @since 5.241101.0
     */
    @SneakyThrows
    public <T extends BaseInfoCodeTackValueLogicModel> T analyzeTackValueLogicModel(@NonNull Class<T> cls) {
        if (Objects.isNull(this.tackValueLogic)) {
            return null;
        }
        return ObjectMapperUtils.readValue(this.tackValueLogic.toString(), new TypeReference<T>() {
            @Override
            public Type getType() {
                return cls;
            }
        });
    }

    /**
     * 解析需求类型取值逻辑模型
     *
     * @return 取值逻辑模型
     * @since 5.240602.0
     */
    @SneakyThrows
    public InfoCodeTackLogicDemandTargetModel parseTackValueLogicModelByDemandType() {
        if (Objects.isNull(tackValueLogic)) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "取值逻辑为空", Pair.of("configId", configId));
            throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_82e22f0258254ac09e4fbe9da012152c"));
        }
        return SpringUtil.getBean(ObjectMapper.class).treeToValue(tackValueLogic,
                InfoCodeTackLogicDemandTargetModel.class);
    }

    /**
     * 解析需求类型取值逻辑模型
     *
     * @return 取值逻辑模型
     * @since 5.240602.0
     */
    @SneakyThrows
    public InfoCodeTackLogicIotPersistenceModel parseTackValueLogicModelByPersistenceTimeType() {
        if (Objects.isNull(tackValueLogic)) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "取值逻辑为空", Pair.of("configId", configId));
            throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_82e22f0258254ac09e4fbe9da012152c"));
        }
        return SpringUtil.getBean(ObjectMapper.class).treeToValue(tackValueLogic,
                InfoCodeTackLogicIotPersistenceModel.class);
    }
}
