package com.persagy.apccontrol.basic.model.dto.systemgraph;


import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class CopyDetailDataParam {

    /**
     * 源副本id
     */
    @NonNull
    private Long sourceDuplicateId;


    /**
     * 目标副本id
     */
    @NonNull
    private Long targetDuplicateId;
}
