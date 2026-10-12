package com.persagy.apccontrol.basic.model.param.outside;

import com.persagy.apccontrol.basic.model.vo.ProjectVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询日出日落时间
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 21:52
 * @since 5.240402.0
 */
@Data
@Schema(title = "查询日出日落时间")
@NoArgsConstructor
@AllArgsConstructor
public class SunRiseAndSetParam extends ProjectVO {

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

}
