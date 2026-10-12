package com.persagy.apccontrol.basic.utils;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.persagy.apccontrol.basic.model.dto.ObjectIotData;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 按日的计数器
 * 主要记录 MQTT 每日接受和发送了多少数据
 */
@Slf4j
public class DailyStringCounter {
    // 用于刷新计数器的全局锁
    private static final ReentrantLock refreshLock = new ReentrantLock();
    // 用于存储每个字符串的计数
    private static Map<String, Integer> counter;
    // 用于存储每个字符串对应的锁
    private static Map<String, ReentrantLock> locks;
    // 日期格式化器，用于获取当前日期
    private static SimpleDateFormat dateFormat;
    // 记录当前日期
    private static String currentDate;
    //是否开启计数的开关
    public static boolean SWITCH = true;
    // sse处理耗时 存储消息唯一标识与开始时间的映射
    private static final ConcurrentHashMap<String, Long> startTimeMap = new ConcurrentHashMap<>();
    //超时阈值 默认 超过 1 秒记录
    public static long timeout = 1000;
    // 使用SHA-256替代MD5，提升哈希安全性和性能
    public static MessageDigest digest;
    
    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        initializeIfNeeded();
    }

    // 检查并初始化必要的对象
    private static void initializeIfNeeded() {
        if (counter == null) {
            refreshLock.lock();
            try {
                if (counter == null) {
                    counter = new HashMap<>();
                    locks = new HashMap<>();
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    currentDate = getCurrentDate();
                }
            } finally {
                refreshLock.unlock();
            }
        }
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期的字符串表示
     */
    private static String getCurrentDate() {
        initializeIfNeeded();
        return dateFormat.format(new Date());
    }

    /**
     * 如果是新的一天，刷新计数器
     */
    private static void refreshCounterIfNewDay() {
        initializeIfNeeded();
        String today = getCurrentDate();
        if (!today.equals(currentDate)) {
            refreshLock.lock();
            try {
                if (!today.equals(currentDate)) {
                    counter.clear();
                    locks.clear();
                    currentDate = today;
                }
            } finally {
                refreshLock.unlock();
            }
        }
    }

    /**
     * 获取对应字符串的锁
     *
     * @param str 要获取锁的字符串
     * @return 对应的锁
     */
    private static ReentrantLock getLock(String str) {
        initializeIfNeeded();
        return locks.computeIfAbsent(str, k -> new ReentrantLock());
    }

    /**
     * 增加指定字符串的计数
     *
     * @param str 要计数的字符串
     */
    public static void incrementCount(String str) {

        if (!SWITCH) {
            return;
        }
        try {
            initializeIfNeeded();
            refreshCounterIfNewDay();
            ReentrantLock lock = getLock(str);
            lock.lock();
            try {
                counter.put(str, counter.getOrDefault(str, 0) + 1);
            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("统计 SSE处理总数处理异常", e);
        }
    }

    /**
     * 获取字符串的计数
     *
     * @return 计数
     */
    public static Map<String, Integer> getCount() {
        return counter;
    }
    
    
    // ---------------- 统计 SSE 消息处理耗时逻辑开始  --------------------------------
    
    /**
     * 生成消息唯一标识（优化版哈希算法）
     */
    public static String generateMessageId(String message) {
        if (digest == null || StringUtil.isBlank(message)) {
            return message;
        }
        byte[] hashBytes = digest.digest((message).getBytes());
        // 使用Base64编码缩短哈希长度（替代十六进制转换）
        return Base64.getEncoder().encodeToString(hashBytes);
    }
    
    /**
     * 统计 SSE 消息处理耗时
     * 消息处理入口（多线程调用）
     */
    public static void sseExecuteElapsedTimeBegin(Object message) {
        if (!SWITCH || message == null) {
            return;
        }
        try {
            String messageId = null;
            if (message instanceof FeedbackInfoMsgMqttVO) {
                ObjectIotData objectIotData = BeanUtil.copyProperties(message, ObjectIotData.class);
                messageId = generateMessageId(getKey(objectIotData));
            } else if (message instanceof String) {
                messageId = (String) message;
            }
            
            if (StringUtil.isBlank(messageId)) {
                return;
            }
            startTimeMap.put(messageId, System.currentTimeMillis());
            
            //如果数量超过 10 万就做一次数据清理
            if (startTimeMap.size() > 100000) {
                startTimeMap.clear();
            }
        } catch (Exception e) {
            log.error("统计 SSE 消息开始处理耗时异常", e);
        }
        
    }
    
    /**
     * 统计 SSE 消息处理耗时
     * 返回处理耗时（毫秒）
     */
    public static Long sseExecuteElapsedTimeEnd(Object message) {
        if (!SWITCH || message == null) {
            return null;
        }
        try {
            String messageId = null;
            if (message instanceof ObjectIotData) {
                messageId = generateMessageId(getKey((ObjectIotData) message));
            } else if (message instanceof String) {
                messageId = (String) message;
            }
            
            if (StringUtil.isBlank(messageId)) {
                return null;
            }
            Long startTime = startTimeMap.remove(messageId);
            if (startTime != null) {
                long t = System.currentTimeMillis() - startTime;
                if (t >= timeout) {
                    return t;
                }
                return null;
            }
        } catch (Exception e) {
            log.error("统计 SSE 消息结束处理耗时异常", e);
        }
        return null;
    }
    
    private static String getKey(ObjectIotData objectIotData) {
        return objectIotData.getObjectId() + objectIotData.getInfoCode() + objectIotData.getDataValue() + objectIotData.getDataTime();
    }
    // ---------------- 统计 SSE 消息处理耗时逻辑结束  --------------------------------
    
}