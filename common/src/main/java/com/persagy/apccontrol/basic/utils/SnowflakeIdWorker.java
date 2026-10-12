package com.persagy.apccontrol.basic.utils;

import cn.hutool.core.bean.BeanException;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 雪花算法 生成唯一ID
 *
 * @author yuyao
 */
public class SnowflakeIdWorker {
    // ==============================Fields===========================================

    /**
     * 机器id所占的位数
     */
    private final long workerIdBits = 5L;

    /**
     * 数据标识id所占的位数
     */
    private final long datacenterIdBits = 5L;

    /**
     * 工作机器ID(0~31)
     */
    private final long workerId;

    /**
     * 数据中心ID(0~31)
     */
    private final long datacenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;
    
    private static final SnowflakeIdWorker INSTANCE = new SnowflakeIdWorker();

    //==============================Constructors=====================================

    /**
     * 构造函数
     *
     */
    public SnowflakeIdWorker() {
        this.workerId = getMachineWorkerId();
        this.datacenterId = getCpuDatacenterId();
    }
    
    // 新增机器特征获取逻辑
    private static long getMachineWorkerId() {
        try {
            // 用机器MAC地址的哈希值作为workerId
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();
            return Math.abs(Arrays.hashCode(mac) % 32); // 0~31
        } catch (Exception e) {
            return ThreadLocalRandom.current().nextLong(0, 32); //  fallback
        }
    }
    
    private static long getCpuDatacenterId() {
        try {
            // 用CPU序列号/核心数作为datacenterId
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            return osBean.getAvailableProcessors() % 32; // 0~31
        } catch (Exception e) {
            return ThreadLocalRandom.current().nextLong(0, 32); // fallback
        }
    }

    
    // ==============================Methods==========================================

    /**
     * 获取默认ID
     *
     * @return long
     */
    public static String getWorkerId() {
        return String.valueOf(INSTANCE.nextId());
    }

    /**
     * 获取默认ID
     *
     * @return long
     */
    public static long getWorkerIdByLong() {
        return INSTANCE.nextId();
    }

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new BeanException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        /* 序列在id中占的位数 */
        long sequenceBits = 12L;
        if (lastTimestamp == timestamp) {
            /* 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
            long sequenceMask = ~(-1L << sequenceBits);
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        /* 时间截向左移22位(5+5+12) */
        long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
        /* 数据标识id向左移17位(12+5) */
        long datacenterIdShift = sequenceBits + workerIdBits;
        /* 机器ID向左移12位 */
        /* 开始时间截 (2015-01-01) */
        long twepoch = 1420041600000L;
        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << sequenceBits)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

}
