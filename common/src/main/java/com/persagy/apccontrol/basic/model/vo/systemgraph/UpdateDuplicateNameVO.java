package com.persagy.apccontrol.basic.model.vo.systemgraph;

import lombok.Data;

/**
 * 更新系统图名称
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/29 10:56
 * @since v5.240802.0
 */
@Data
public class UpdateDuplicateNameVO {

    /**
     * 副本id
     */
    private Long duplicateId;

    /**
     * 副本名称
     */
    private String duplicateName;

}
