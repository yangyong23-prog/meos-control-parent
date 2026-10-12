package com.persagy.apccontrol.basic.config.fastjson.deserializer.zone;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.Jdk8DateCodec;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;

import java.lang.reflect.Type;
import java.time.*;
import java.util.Objects;

/**
 * Jdk8日期带时区反序列化器
 *
 * @author JiYinchuan
 * @since 7.251002.0
 */
public class Jdk8DateWithZoneDeserializer extends Jdk8DateCodec {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName, String format, int feature) {
        T object = super.deserialze(parser, type, fieldName, format, feature);
        if (Objects.nonNull(object)) {
            if (object instanceof LocalTime) {
                return (T) ZonedDateTime.of(LocalDate.now(), (LocalTime) object, ZoneId.systemDefault())
                        .withZoneSameInstant(DateUtils.getZoneId()).toLocalTime();
            }
            if (object instanceof LocalDate) {
                return (T) ZonedDateTime.of((LocalDate) object, LocalTime.MIN, ZoneId.systemDefault())
                        .withZoneSameInstant(DateUtils.getZoneId()).toLocalDate();
            }
            if (object instanceof LocalDateTime) {
                return (T) ZonedDateTime.of((LocalDateTime) object, ZoneId.systemDefault())
                        .withZoneSameInstant(DateUtils.getZoneId()).toLocalDateTime();
            }
        }
        return object;
    }
}
