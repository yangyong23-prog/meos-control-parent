package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 查询-需求变化
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 14:56
 * @since 5.240402.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "查询-需求变化")
public class LightingSidebarDemandChangeVO {

    /**
     * 需求信息
     */
    private List<LightingSidebarDemandResult> lightingSidebarDemandList;

    /**
     * 当日运行模式
     */
    private DemandTemplateVO relTemplate;

}
