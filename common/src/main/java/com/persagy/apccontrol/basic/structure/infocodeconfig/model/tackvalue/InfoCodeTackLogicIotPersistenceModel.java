package com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.lang.NonNull;

/**
 * 信息点需求目标取值逻辑模型
 *
 * @author LiuYang
 * @since 5.240802.0
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InfoCodeTackLogicIotPersistenceModel extends BaseInfoCodeTackValueLogicModel {

    /**
     * 类型
     */
    @NonNull
    private DtpRwdPersistenceTimeTypeEnum timeType;

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "持久化信息点解析逻辑";
    }
}
