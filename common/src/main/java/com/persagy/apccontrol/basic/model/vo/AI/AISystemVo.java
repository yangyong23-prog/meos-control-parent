package com.persagy.apccontrol.basic.model.vo.AI;

import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: wxf
 * @create: 2025-07-18 15:53
 **/
@Data
public class AISystemVo extends DtpRwdEquipmentDTO {

    @Schema(description = "服务的空间")
    private List<DtpRwdSpaceDTO> serviceSpaces;

    @Schema(description = "系统关联的设备,key 是 classCode")
    private Map<String, List<DtpRwdEquipmentDTO>> relEquipments;

    @Schema(description = "系统关联的设备,key 是 classCode")
    private Map<String, DtpKnowledgeInfoCodeDTO> infoCodeDefine;
}