package com.persagy.apccontrol.basic.executor.log;

import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 项目日志执行器
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
public class LogProjectExecutor extends BaseLogExecutor implements LogProvider {

    /**
     * 项目DTO
     */
    private final ProjectDTO projectDTO;

    /**
     * 构造器
     *
     * @param projectDTO 项目DTO
     * @since v5.1.0
     */
    public LogProjectExecutor(@NonNull ProjectDTO projectDTO) {
        super(projectDTO.getGroupCode(), projectDTO.getProjectId());
        this.projectDTO = projectDTO;
    }

    @Override
    public String log(
            @NonNull Logger logger, @NonNull Level level, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable List<Pair<String, Object>> additionalMessages) {
        LinkedList<Pair<String, Object>> originalMessages = new LinkedList<>();
        if (Objects.nonNull(projectDTO)) {
            originalMessages.add(Pair.of("projectLocalId", projectDTO.getProjectLocalId()));
            originalMessages.add(Pair.of("projectLocalName", projectDTO.getProjectLocalName()));
        }
        return super.log(logger, level, logTag, logDetailTag, throwable, ArgArray.builder()
                .originalMessages(originalMessages)
                .additionalMessages(additionalMessages)
                .parse());
    }
}
