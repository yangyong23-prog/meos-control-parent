package com.persagy.apccontrol.basic.controller.shellconfig.param;

import com.persagy.apccontrol.basic.model.entity.ShellConfigGroup;
import lombok.Data;

import java.util.List;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/7 5:27 PM
 */
@Data
public class ShellConfigVO {

    private List<ShellConfigGroup> groups;

    private String desc;
}
