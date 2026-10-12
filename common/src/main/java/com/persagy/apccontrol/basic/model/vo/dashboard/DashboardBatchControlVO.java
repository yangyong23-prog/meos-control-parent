package com.persagy.apccontrol.basic.model.vo.dashboard;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeConfigVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 网格图批量控制VO
 *
 * @author JiYinchuan
 * @since 7.250901.0
 */
@Data
public class DashboardBatchControlVO {

    @Schema(description = "模版类型ID")
    private Long templateTypeId;

    @Schema(description = "模版类型名称")
    private String templateTypeName;

    @Schema(description = "顺序")
    private Integer sequence;

    @Schema(description = "关联信息点配置")
    private List<InfoCodeConfigVO> relInfoCodeConfigs;

    @Schema(description = "信息点 -> 关联对象ID")
    private Map<String, Set<String>> infoCodeAndObjectIds;

    @Schema(description = "对象ID -> 对象数据")
    private Map<String, DtpRwdObjectDTO> objectIdAndData;

}
