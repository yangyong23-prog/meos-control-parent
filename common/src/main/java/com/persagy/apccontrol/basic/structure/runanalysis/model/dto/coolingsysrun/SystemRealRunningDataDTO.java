package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingsysrun;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统实际运行数据DTO
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
public class SystemRealRunningDataDTO {

    /**
     * 关联对象，为空时则表示系统
     */
    private @Nullable DtpRwdObjectDTO relObject;

    /**
     * 对象类型
     */
    private @Nullable ObjectType objectType;

    /**
     * 时间值 -> 数据
     */
    private Map<LocalDateTime, String> dateTime2Value;

    /**
     * 是否有开记录
     */
    private Boolean hasOpenRecord;

    @Getter
    @AllArgsConstructor
    public enum ObjectType implements BaseEnum<String> {

        /**
         * 冷机
         */
        CHILLER("chiller", "冷机"),

        /**
         * 冷冻水泵
         */
        FROZEN_PUMP("frozenPump", "冷冻水泵");

        /**
         * 枚举值
         */
        private final String value;

        /**
         * 枚举信息
         */
        private final String reasonPhrase;

        @Override
        public String value() {
            return this.value;
        }
    }
}
