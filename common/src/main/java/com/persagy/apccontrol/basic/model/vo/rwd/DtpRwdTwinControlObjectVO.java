package com.persagy.apccontrol.basic.model.vo.rwd;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdTwinControlInfoCodeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 物理世界-孪生控制对象VO
 *
 * @author JiYinchuan
 * @since 5.240601.0
 */
@Data
@Schema(title = "物理世界-孪生控制对象DTO")
public class DtpRwdTwinControlObjectVO {

    /**
     * 对象ID
     */
    private String objectId;

    /**
     * 设备类编码
     */
    private String classCode;

    /**
     * 所属系统ID, 如果无系统ID则为对象ID
     */
    private String systemId;

    /**
     * 信息的列表
     */
    private Map<String,DtpRwdTwinControlInfoCodeDTO> infoCodesMap;

}
