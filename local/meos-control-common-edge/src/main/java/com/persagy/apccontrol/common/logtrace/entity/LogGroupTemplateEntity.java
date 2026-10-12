package com.persagy.apccontrol.common.logtrace.entity;

import lombok.Data;

import java.util.List;

@Data
public class LogGroupTemplateEntity {

    private Long id;

    private int type;

    private int orderNum;

    private String name;

    private String content;

    private List<LogGroupTemplateEntity> children;

    public static void traverseTree(LogGroupTemplateEntity node) {
        if (node != null) {
            // 递归遍历子节点列表
            List<LogGroupTemplateEntity> children = node.getChildren();
            if (children != null) {
                for (LogGroupTemplateEntity child : children) {
                    traverseTree(child); // 递归调用
                }
            }
        }
    }
}
