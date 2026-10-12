package com.persagy.apccontrol.basic.model.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 本地项目基础项目信息
 *
 * @author yuyao
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalityProjectCfgVO {

    /**
     * 集团编码
     */
    private String groupCode;
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 本地服务地址
     */
    private String localityIp;
    /**
     * 本地服务端口
     */
    private String localityPort;

}
