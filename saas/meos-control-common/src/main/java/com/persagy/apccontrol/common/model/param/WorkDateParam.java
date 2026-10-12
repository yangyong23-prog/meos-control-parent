package com.persagy.apccontrol.common.model.param;

import com.persagy.apccontrol.common.model.vo.ProjectVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/12/6 14:32
 */
@Getter
@Setter
@ToString(callSuper = true)
public class WorkDateParam extends ProjectVO {

    @NotNull(message = "{key_57996be8bc8c463395e3521d6ee68562}")
    @Schema(description = "日期")
    private LocalDate workDate;

}
