package com.persagy.apccontrol.basic.model.vo.duty;

import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChildCardDataVO extends BasicsChildCardDataVO {

    @Schema(description = "卡片名字")
    private String name;
    @Schema(description = "需求模版")
    private DemandTemplateVO demandTemplate;
    @Schema(description = "设备总数")
    private Integer equipmentTotal;
    @Schema(description = "运行总数")
    private Integer runningTotal;
    @Schema(description = "干预总数")
    private int interveneTotal;
    @Schema(description = "少开数量")
    private int lessOpenTotal;
    @Schema(description = "多开数量")
    private int moreOpenTotal;

    @Schema(description = "是否可跳转")
    private Boolean canJump;


    /**
     * 获取名称
     *
     * @return 卡片名称
     */
    public String getName() {
        if (StringUtils.isNotEmpty(name) && name.contains(CommonConst.COLON)) {
            return name.substring(name.indexOf(CommonConst.COLON) + 1);
        }
        return name;
    }

    /**
     * 获取名称编码
     *
     * @return 名称编码
     */
    public String getNameCode() {
        if (StringUtils.isNotEmpty(name) && name.contains(CommonConst.COLON)) {
            return name.substring(0, name.indexOf(CommonConst.COLON));
        }
        return name;
    }
}
