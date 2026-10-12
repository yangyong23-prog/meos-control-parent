package com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue;

import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Set;

/**
 * IOT信息点判断取值逻辑模型
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InfoCodeTackLogicIotInfoCodeHandlerModel extends BaseInfoCodeTackValueLogicModel {

    /**
     * 信息点编码
     */
    @NonNull
    private Set<String> infoCodes;

    /**
     * SPEL表达式
     */
    @NonNull
    private String takeValueExpression;

    /**
     * 查询ID条件
     */
    @NonNull
    private DtpRwdObjectQueryParam queryIdParam;

    /**
     * 占位符数据源
     */
    private Map<String, DtpRwdObjectQueryParam> idSource;

    @Override
    public String logTag() {
        return "信息点配置-取值逻辑-IOT信息点处理";
    }
}
