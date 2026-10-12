package com.persagy.apccontrol.basic.model.vo.systemgraph;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author mj
 * @date 2022/10/11 17:42
 */
@Data
public class BaseColorVo {
    private String color = "#FFFFFF";
    private String showName = LocaleMessageUtil.getMessage("key_167363685e864e949bc8cee45b5b61c3");
    private BigDecimal alpha = new BigDecimal(1);
}
