package com.persagy.apccontrol.basic.model.dto.rwd;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdControlInfoCodeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 物理世界-孪生控制信息点DTO
 *
 * @author JiYinchuan
 * @since 5.240601.0
 */
@Data
@Schema(title = "物理世界-孪生控制信息点DTO")
public class DtpRwdTwinControlInfoCodeDTO {

    /**
     * 信息点编码
     */
    private String code;

    /**
     * 信息的名称
     */
    private String name;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 点位类型
     */
    private DtpRwdControlInfoCodeTypeEnum infoCodeType;

}
