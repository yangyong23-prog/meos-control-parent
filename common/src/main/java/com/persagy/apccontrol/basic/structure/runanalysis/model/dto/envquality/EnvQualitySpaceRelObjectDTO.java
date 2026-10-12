package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.envquality;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 环境品质-空间关联对象 DTO
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
public class EnvQualitySpaceRelObjectDTO {

    /**
     * 关联对象
     */
    private DtpRwdObjectDTO object;

    /**
     * 信息点-时间-值
     */
    private Map<String, Map<LocalDateTime, String>> infoCode2DateTime2Value;

}
