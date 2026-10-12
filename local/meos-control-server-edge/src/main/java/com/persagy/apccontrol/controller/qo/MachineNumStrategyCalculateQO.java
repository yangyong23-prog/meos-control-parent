package com.persagy.apccontrol.controller.qo;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Description:   
 * Company: Persagy 
 * @author hengxi 
 * @version 1.0
 * @since: 2023年12月18日: 下午5:59:40
 * Update By hengxi 2023年12月18日: 下午5:59:40
 */
@Data
public class MachineNumStrategyCalculateQO {
	
	@NotBlank(message = "{key_b7d8ae82246f4d7f9790c7adb3fa619f}")
	@Schema(title = "集团编码", required = true,example = "TYIFC")
	private String groupCode;
	
	@NotBlank(message = "{key_2c5faf38144c4309a39cbea7cd5f4a04}")
	@Schema(title = "项目id", required = true,example = "Pj7763494626")
	private String projectId;
	
	@NotBlank(message = "{key_0673e64c60284dc89276275e81c698f6}")
	@Schema(title = "系统id", required = true,example = "Sy1694252386249756674")
	private String systemId;
}
