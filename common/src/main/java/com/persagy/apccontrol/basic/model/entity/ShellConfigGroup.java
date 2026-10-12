package com.persagy.apccontrol.basic.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/7 8:19 PM
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class ShellConfigGroup {

    private String group;
    private String tab;
    private List<ShellConfigItem> items;
}
