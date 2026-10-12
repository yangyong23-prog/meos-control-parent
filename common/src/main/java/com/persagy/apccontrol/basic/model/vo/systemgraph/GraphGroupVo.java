package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphGroupDto;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.SystemGraphType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 *
 * @author houyuanzhi
 * @date 2023/9/11 18:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GraphGroupVo extends GraphGroupDto {
    private List<GraphLegendVo> legendUrlVo;

    /**
     * 别名
     */
    @Schema(description = "别名")
    private String aliasName;

    @Schema(description = "滤镜状态对象")
    private FilterStatusVO filterStatusVO;

    @Schema(description = "滤镜状态")
    private String filterStatus;

    @Schema(description = "信息点配置列表")
    private List<RcInfoCodeCfg> fixedInfoCodes;

    private SystemGraphType systemGraphType = SystemGraphType.NONE;

    /** web自定义数据,前端存储，前端消费 */
    private Object webCustomData;
}
