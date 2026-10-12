package com.persagy.apccontrol.basic.executor.log;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.validate.utility.exception.ApplicationException;
import com.xxl.job.core.context.XxlJobHelper;
import feign.FeignException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * 日志提供器
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
public interface LogProvider {

    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @return 日志字符串
     * @since v5.1.0
     */
    default String trace(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag) {
        return this.trace(logger, logTag, logDetailTag, null, (List<Pair<String, Object>>) null);
    }

    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @param throwable    原始异常
     * @return 日志字符串
     * @since v5.1.0
     */
    default String trace(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable) {
        return this.trace(logger, logTag, logDetailTag, throwable, (List<Pair<String, Object>>) null);
    }

    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String trace(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Pair<String, Object> additionalMessage) {
        return this.trace(logger, logTag, logDetailTag, null, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since 5.240501.0
     */
    @SuppressWarnings("unused")
    default String trace(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Pair<String, Object>... additionalMessages) {
        return this.trace(logger, logTag, logDetailTag, null, getAdditionalMessages(additionalMessages));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String trace(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable List<Pair<String, Object>> additionalMessages) {
        return this.trace(logger, logTag, logDetailTag, null, additionalMessages);
    }

    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param throwable         原始异常
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String trace(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable Pair<String, Object> additionalMessage) {
        return this.trace(logger, logTag, logDetailTag, throwable, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param throwable          原始异常
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String trace(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable List<Pair<String, Object>> additionalMessages) {
        return this.log(logger, Level.TRACE, logTag, logDetailTag, throwable, additionalMessages);
    }

    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @return 日志字符串
     * @since v5.1.0
     */
    default String debug(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag) {
        return this.debug(logger, logTag, logDetailTag, null, (List<Pair<String, Object>>) null);
    }

    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @param throwable    原始异常
     * @return 日志字符串
     * @since v5.1.0
     */
    default String debug(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable) {
        return this.debug(logger, logTag, logDetailTag, throwable, (List<Pair<String, Object>>) null);
    }

    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String debug(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Pair<String, Object> additionalMessage) {
        return this.debug(logger, logTag, logDetailTag, null, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String debug(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable List<Pair<String, Object>> additionalMessages) {
        return this.debug(logger, logTag, logDetailTag, null, additionalMessages);
    }

    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param throwable         原始异常
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String debug(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable Pair<String, Object> additionalMessage) {
        return this.debug(logger, logTag, logDetailTag, throwable, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param throwable          原始异常
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String debug(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable List<Pair<String, Object>> additionalMessages) {
        return this.log(logger, Level.DEBUG, logTag, logDetailTag, throwable, additionalMessages);
    }

    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @return 日志字符串
     * @since v5.1.0
     */
    default String info(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag) {
        return this.info(logger, logTag, logDetailTag, null, (List<Pair<String, Object>>) null);
    }

    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @param throwable    原始异常
     * @return 日志字符串
     * @since v5.1.0
     */
    default String info(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable) {
        return this.info(logger, logTag, logDetailTag, throwable, (List<Pair<String, Object>>) null);
    }

    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String info(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Pair<String, Object> additionalMessage) {
        return this.info(logger, logTag, logDetailTag, null, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String info(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable List<Pair<String, Object>> additionalMessages) {
        return this.info(logger, logTag, logDetailTag, null, additionalMessages);
    }

    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param throwable         原始异常
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String info(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable Pair<String, Object> additionalMessage) {
        return this.info(logger, logTag, logDetailTag, throwable, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param throwable          原始异常
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String info(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable List<Pair<String, Object>> additionalMessages) {
        return this.log(logger, Level.INFO, logTag, logDetailTag, throwable, additionalMessages);
    }

    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @return 日志字符串
     * @since v5.1.0
     */
    default String warn(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag) {
        return this.warn(logger, logTag, logDetailTag, null, (List<Pair<String, Object>>) null);
    }

    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @param throwable    原始异常
     * @return 日志字符串
     * @since v5.1.0
     */
    default String warn(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable) {
        return this.warn(logger, logTag, logDetailTag, throwable, (List<Pair<String, Object>>) null);
    }

    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String warn(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Pair<String, Object> additionalMessage) {
        return this.warn(logger, logTag, logDetailTag, null, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String warn(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable List<Pair<String, Object>> additionalMessages) {
        return this.warn(logger, logTag, logDetailTag, null, additionalMessages);
    }

    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param throwable         原始异常
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String warn(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable Pair<String, Object> additionalMessage) {
        return this.warn(logger, logTag, logDetailTag, throwable, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param throwable          原始异常
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String warn(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable List<Pair<String, Object>> additionalMessages) {
        return this.log(logger, Level.WARN, logTag, logDetailTag, throwable, additionalMessages);
    }

    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @return 日志字符串
     * @since v5.1.0
     */
    default String error(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag) {
        return this.error(logger, logTag, logDetailTag, null, (List<Pair<String, Object>>) null);
    }


    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @param throwable    原始异常
     * @return 日志字符串
     * @since v5.1.0
     */
    default String error(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable) {
        return this.error(logger, logTag, logDetailTag, throwable, (List<Pair<String, Object>>) null);
    }


    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String error(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Pair<String, Object> additionalMessage) {
        return this.error(logger, logTag, logDetailTag, null, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String error(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable List<Pair<String, Object>> additionalMessages) {
        return this.error(logger, logTag, logDetailTag, null, additionalMessages);
    }

    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param throwable         原始异常
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String error(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable Pair<String, Object> additionalMessage) {
        return this.error(logger, logTag, logDetailTag, throwable, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param throwable          原始异常
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String error(
            @NonNull Logger logger, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable List<Pair<String, Object>> additionalMessages) {
        return this.log(logger, Level.ERROR, logTag, logDetailTag, throwable, additionalMessages);
    }

    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param level        日志级别
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @return 日志字符串
     * @since v5.1.0
     */
    default String log(
            @NonNull Logger logger, @NonNull Level level,
            @NonNull String logTag, @NonNull String logDetailTag) {
        return this.log(logger, level, logTag, logDetailTag, null, (List<Pair<String, Object>>) null);
    }

    /**
     * 日志打印
     *
     * @param logger       日志源
     * @param level        日志级别
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @param throwable    原始异常
     * @return 日志字符串
     * @since v5.1.0
     */
    default String log(
            @NonNull Logger logger, @NonNull Level level,
            @NonNull String logTag, @NonNull String logDetailTag,
            @Nullable Throwable throwable) {
        return this.log(logger, level, logTag, logDetailTag, throwable, (List<Pair<String, Object>>) null);
    }

    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param level             日志级别
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String log(
            @NonNull Logger logger, @NonNull Level level, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Pair<String, Object> additionalMessage) {
        return this.log(logger, level, logTag, logDetailTag, null, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param level              日志级别
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String log(
            @NonNull Logger logger, @NonNull Level level, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable List<Pair<String, Object>> additionalMessages) {
        return this.log(logger, level, logTag, logDetailTag, null, additionalMessages);
    }

    /**
     * 日志打印
     *
     * @param logger            日志源
     * @param level             日志级别
     * @param logTag            日志标签
     * @param logDetailTag      日志详细标签
     * @param throwable         原始异常
     * @param additionalMessage 附加信息
     * @return 日志字符串
     * @since 5.240402.0
     */
    default String log(
            @NonNull Logger logger, @NonNull Level level, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable Pair<String, Object> additionalMessage) {
        return this.log(logger, level, logTag, logDetailTag, throwable, getAdditionalMessages(additionalMessage));
    }

    /**
     * 日志打印
     *
     * @param logger             日志源
     * @param level              日志级别
     * @param logTag             日志标签
     * @param logDetailTag       日志详细标签
     * @param throwable          原始异常
     * @param additionalMessages 附加信息
     * @return 日志字符串
     * @since v5.1.0
     */
    default String log(
            @NonNull Logger logger, @NonNull Level level, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable List<Pair<String, Object>> additionalMessages) {
        StringJoiner joiner = new StringJoiner(", ");
        if (Objects.nonNull(additionalMessages)) {
            for (Pair<String, Object> additionalMessage : additionalMessages) {
                joiner.add(additionalMessage.getLeft() + ": " + additionalMessage.getRight());
            }
        }
        String messagePattern;
        Object[] argArray;
        if (throwable instanceof BusinessException) {
            BusinessException exception = (BusinessException) throwable;
            messagePattern = "[{}] {} [{}, errorCode: {}, errorMessage: {}]";
            argArray = Lists.newArrayList(logTag, logDetailTag, joiner.toString(),
                    exception.getCode(), exception.getLocalizedMessage()).toArray();
            throwable = null;
        } else if (throwable instanceof ApplicationException) {
            ApplicationException exception = (ApplicationException) throwable;
            messagePattern = "[{}] {} [{}, errorCode: {}, errorMessage: {}, userTip: {}]";
            argArray = Lists.newArrayList(logTag, logDetailTag, joiner.toString(),
                    exception.getErrorCode(), exception.getErrorMessage(), exception.getUserTip()).toArray();
            throwable = null;
        } else if (throwable instanceof FeignException) {
            messagePattern = "[{}] {} [{}, errorMessage: {}]";
            argArray = Lists.newArrayList(logTag, logDetailTag, joiner.toString(), throwable.getLocalizedMessage()).toArray();
            throwable = null;
        } else {
            messagePattern = "[{}] {} [{}]";
            argArray = Lists.newArrayList(logTag, logDetailTag, joiner.toString()).toArray();
        }
        String message = MessageFormatter.arrayFormat(messagePattern, argArray, throwable).getMessage();
        switch (level) {
            case TRACE:
                logger.trace(message, throwable);
                break;
            case DEBUG:
                logger.debug(message, throwable);
                break;
            case INFO:
                logger.info(message, throwable);
                break;
            case WARN:
                logger.warn(message, throwable);
                break;
            case ERROR:
                logger.error(message, throwable);
                break;
            default:
        }
        if (level.toInt() >= Level.INFO.toInt()) {
            XxlJobHelper.log(message);
            if (Objects.nonNull(throwable)) {
                XxlJobHelper.log(throwable);
            }
        }
        if (LocalContextStorage.isRequestDebug(level)) {
            LocalContextStorage.putRequestDebug(logTag, logDetailTag, message);
        }
        return message;
    }

    /**
     * 获取附加信息
     *
     * @param additionalMessage 附加信息
     * @return 附加信息
     * @since 5.240501.0
     */
    default List<Pair<String, Object>> getAdditionalMessages(@Nullable Pair<String, Object> additionalMessage) {
        return Objects.nonNull(additionalMessage) ? Collections.singletonList(additionalMessage) : null;
    }

    /**
     * 获取附加信息
     *
     * @param additionalMessages 附加信息
     * @return 附加信息
     * @since 5.240501.0
     */
    @SuppressWarnings("unchecked")
    default List<Pair<String, Object>> getAdditionalMessages(@Nullable Pair<String, Object>... additionalMessages) {
        return Objects.nonNull(additionalMessages) ? Arrays.asList(additionalMessages) : null;
    }
}
