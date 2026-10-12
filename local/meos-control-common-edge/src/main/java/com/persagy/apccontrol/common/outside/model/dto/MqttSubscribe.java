package com.persagy.apccontrol.common.outside.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author shaohongbo
 * @date 2023年03月29日 20:04
 */
@Data
public class MqttSubscribe {

    private String classCode;

    private List<String> infoCode;

}
