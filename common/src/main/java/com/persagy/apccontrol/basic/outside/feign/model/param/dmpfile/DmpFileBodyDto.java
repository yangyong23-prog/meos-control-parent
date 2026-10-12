package com.persagy.apccontrol.basic.outside.feign.model.param.dmpfile;

import com.google.common.collect.Sets;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件接口body
 *
 * @author 叶丁辉
 * @date 2022/6/24 14:48
 */
@Data
public class DmpFileBodyDto {

    /**
     * 文件id集合
     */
    private Set<String> fileIds;


    public DmpFileBodyDto setFileIds(Collection<String> fileIds) {
        if (CollectionUtils.isNotEmpty(fileIds)) {
            this.fileIds = Sets.newHashSet(fileIds);
        }
        return this;
    }

    public DmpFileBodyDto setFileIds(String... fileIds) {
        if (fileIds != null && fileIds.length > 0 && Stream.of(fileIds).anyMatch(Objects::nonNull)) {
            this.fileIds = Stream.of(fileIds).filter(Objects::nonNull).collect(Collectors.toSet());
        }
        return this;
    }
}
