package com.persagy.apccontrol.basic.structure.infocodeconfig.context;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.iot.IotConverter;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 信息点配置-IOT数据上下文
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeIotDataContext {

    // ------------------------- 入参 -------------------------

    /**
     * 聚合根
     */
    private final Collection<InfoCodeAggregate> aggregates;

    // ------------------------- 过程 -------------------------

    /**
     * IOT信息点值
     */
    private List<InfoCodeValueVO> infoCodeValues = Collections.emptyList();

    /**
     * 对象ID -> [IOT设定信息点 -> 恢复时间]
     */
    private Map<String, Map<String, LocalDateTime>> objectIdIotSetInfoCodeAndRecoverTime = Collections.emptyMap();

    /**
     * 填充聚合根信息点值模型
     *
     * @since 5.240602.0
     */
    public void fillAggregateInfoCodeDataModel(@NonNull List<InfoCodeAggregate> aggregates) {
        IotConverter iotConverter = SpringUtil.getBean(IotConverter.class);
        // 对象ID+[信息点编码 -> 信息点值] 为了解决一个信息点在多个分组上无法被正确赋值
        Map<String, InfoCodeValueVO> infoCodeValueVOMap = new HashMap<>(16);
        for (InfoCodeValueVO infoCodeValue : infoCodeValues) {
            String objectId = infoCodeValue.getObjectId();
            String infoCode = infoCodeValue.getInfoCode();
            infoCodeValueVOMap.put(objectId + infoCode, infoCodeValue);
        }
        for (InfoCodeAggregate aggregate : aggregates) {
            String infoCode = aggregate.getIdentifier().getInfoCode();
            String objectId = aggregate.getIdentifier().getObject().getId();
            Map<String, Object> additionalMapValue = new LinkedHashMap<>();
            LocalDateTime iotSetInfoCodeRecoverTime = objectIdIotSetInfoCodeAndRecoverTime
                    .getOrDefault(objectId, Collections.emptyMap()).get(infoCode);
            if (Objects.nonNull(iotSetInfoCodeRecoverTime)) {
                additionalMapValue.put("recoveryDateTime", iotSetInfoCodeRecoverTime);
            }
            InfoCodeValueVO infoCodeValue = infoCodeValueVOMap.get(objectId + infoCode);
            if (Objects.nonNull(infoCodeValue)) {
                aggregate.setInfoCodeAndDataModel(iotConverter.toInfoCodeDataModel(infoCodeValue, additionalMapValue));
            }
        }
    }
}
