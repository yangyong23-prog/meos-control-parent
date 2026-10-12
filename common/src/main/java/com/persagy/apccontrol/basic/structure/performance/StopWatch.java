/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.persagy.apccontrol.basic.structure.performance;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Simple stop watch, allowing for timing of a number of tasks, exposing total
 * running time and running time for each named task.
 *
 * <p>Conceals use of {@link System#nanoTime()}, improving the readability of
 * application code and reducing the likelihood of calculation errors.
 *
 * <p>Note that this object is not designed to be thread-safe and does not use
 * synchronization.
 *
 * <p>This class is normally used to verify performance during proof-of-concept
 * work and in development, rather than as part of production applications.
 *
 * <p>Running time is tracked and reported in nanoseconds. As of Spring Framework
 * 6.1, the default time unit for String renderings is seconds with decimal points
 * in nanosecond precision. Custom renderings with specific time units can be
 * requested through {@link #prettyPrint(TimeUnit)}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see #create(String)
 * @see #addTask(StopWatch)
 * @see #addTaskGroup(StopWatch)
 * @see #addAsyncTaskGroup(String, TaskGroup)
 * @see #addAsyncTaskGroup(String, TaskGroup...)
 * @see #addAsyncTaskGroup(String, List)
 * @see #start()
 * @see #stop()
 * @see #shortSummary()
 * @see #prettyPrint()
 * @since May 2, 2001
 */
public class StopWatch {

    /**
     * Identifier of this {@code StopWatch}.
     * <p>Handy when we have output from multiple stop watches and need to
     * distinguish between them in log or console output.
     */
    @Getter
    private final String id;

    private @Nullable List<TaskInfo> taskList = new ArrayList<>(1);

    /**
     * Start time of the current task.
     */
    private long startTimeNanos;

    /**
     * Name of the current task.
     */
    private @Nullable String currentTaskName;

    private @Nullable TaskInfo lastTaskInfo;

    @Getter
    private int taskCount;

    @Getter
    private long totalTimeNanos;

    // ------------------------- 自定义方法开始 -------------------------

    /**
     * 分组中第一个任务名称 -> 对应分组名称
     */
    private final Map<String, String> taskNameAndNextGroupName = new ConcurrentHashMap<>();

    /**
     * 分组名称 -> 分组下任务
     */
    private final Map<String, List<TaskInfo>> taskGroupNameAndTasks = new ConcurrentHashMap<>();

    /**
     * 异步任务列表
     */
    private final List<Set<String>> asyncTaskNames = Collections.synchronizedList(new ArrayList<>());

    @AllArgsConstructor
    public static class TaskGroup {
        private final String taskName;
        private final Runnable runnable;
    }

    /**
     * 创建计时任务（秒表）
     *
     * @return StopWatch
     * @since 7.250302.0
     */
    public static StopWatch create() {
        return new StopWatch("");
    }

    /**
     * 创建计时任务（秒表）
     *
     * @param id 用于标识秒表的唯一ID
     * @return StopWatch
     * @since 7.250302.0
     */
    public static StopWatch create(@NonNull String id) {
        return new StopWatch(id);
    }

    /**
     * 添加任务
     *
     * @param task 任务
     * @since 7.250302.0
     */
    public synchronized void addTask(@NonNull StopWatch task) {
        this.totalTimeNanos += task.totalTimeNanos;
        if (CollectionUtils.isNotEmpty(task.taskList)) {
            addTaskInfo(task.taskList);
        }
    }

    /**
     * 添加任务组
     *
     * @param task 任务
     * @since 7.250302.0
     */
    public synchronized void addTaskGroup(@NonNull StopWatch task) {
        addTaskGroup(task.id, task);
    }

    /**
     * 添加任务组
     *
     * @param id   任务ID
     * @param task 任务
     * @since 7.250302.0
     */
    public synchronized void addTaskGroup(@NonNull String id, @NonNull StopWatch task) {
        id = id + Thread.currentThread().getId();
        this.totalTimeNanos += task.totalTimeNanos;
        if (CollectionUtils.isNotEmpty(task.taskList)) {
            this.taskGroupNameAndTasks.put(id, task.taskList);
            this.taskNameAndNextGroupName.put(task.taskList.get(0).taskName, id);
            this.asyncTaskNames.addAll(task.asyncTaskNames);
            addTaskInfo(task.taskList);
        }
    }

    /**
     * 添加异步任务组
     *
     * @param taskGroupName 任务组名称
     * @param task          任务
     * @since 7.250302.0
     */
    public synchronized void addAsyncTaskGroup(@NonNull String taskGroupName, StopWatch.@NonNull TaskGroup task) {
        addAsyncTaskGroup(taskGroupName, Collections.singletonList(task));
    }

    /**
     * 添加异步任务组
     *
     * @param taskGroupName 任务组名称
     * @param tasks         任务
     * @since 7.250302.0
     */
    public synchronized void addAsyncTaskGroup(@NonNull String taskGroupName, @NonNull TaskGroup... tasks) {
        addAsyncTaskGroup(taskGroupName, ArrayUtils.isNotEmpty(tasks) ? Arrays.asList(tasks) : null);
    }

    /**
     * 添加异步任务组
     *
     * @param taskGroupName 任务组名称
     * @param tasks         任务
     * @since 7.250302.0
     */
    public synchronized void addAsyncTaskGroup(
            @NonNull String taskGroupName,
            @Nullable List<TaskGroup> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        List<TaskInfo> localTaskInfos = Collections.synchronizedList(new ArrayList<>());
        long startTimeNanos = System.nanoTime();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Set<String> asyncTaskNames = new LinkedHashSet<>();
        for (TaskGroup task : tasks) {
            asyncTaskNames.add(task.taskName);
            futures.add(CompletableFuture.runAsync(() -> {
                StopWatch watch = StopWatch.create(task.taskName);
                watch.start(task.taskName);
                task.runnable.run();
                watch.stop();
                localTaskInfos.add(watch.lastTaskInfo());
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long lastTime = System.nanoTime() - startTimeNanos;
        this.totalTimeNanos += lastTime;
        this.taskGroupNameAndTasks.put(taskGroupName, localTaskInfos);
        this.taskNameAndNextGroupName.put(localTaskInfos.get(0).taskName, taskGroupName);
        this.asyncTaskNames.add(asyncTaskNames);
        addTaskInfo(localTaskInfos);
    }

    /**
     * 带分组的打印
     *
     * @see #prettyPrint()
     * @since 7.250601.0
     */
    public String prettyPrintByGroup() {
        return prettyPrintByGroup(TimeUnit.SECONDS);
    }

    /**
     * 带分组的打印
     *
     * @param timeUnit the unit to use for rendering total time and task time
     * @see #prettyPrint()
     * @since 7.250302.0
     */
    @SuppressWarnings("DuplicatedCode")
    public String prettyPrintByGroup(TimeUnit timeUnit) {
        if (MapUtils.isEmpty(this.taskGroupNameAndTasks)) {
            return prettyPrint(timeUnit);
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(9);
        nf.setGroupingUsed(false);

        NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
        pf.setMinimumIntegerDigits(2);
        pf.setGroupingUsed(false);

        StringBuilder sb = new StringBuilder(128);
        sb.append("StopWatch '").append(getId()).append("': ");
        String total = (timeUnit == TimeUnit.NANOSECONDS ?
                nf.format(getTotalTimeNanos()) : nf.format(getTotalTime(timeUnit)));
        sb.append(total).append(" ").append(timeUnit.name().toLowerCase(Locale.ENGLISH));
        int width = Math.max(sb.length(), 40);
        sb.append("\n");

        StringBuilder lineBuilder = new StringBuilder();
        for (int i = 0; i < width; i++) {
            lineBuilder.append('-');
        }
        String line = lineBuilder + "\n";
        String unitName = timeUnit.name();
        unitName = unitName.charAt(0) + unitName.substring(1).toLowerCase(Locale.ENGLISH);
        unitName = String.format("%-12s", unitName);
        sb.append(line);
        sb.append(unitName).append("  %       Task name\n");
        sb.append(line);
        boolean fistGroup = true;
        Set<String> executedTaskNames = new HashSet<>();
        Map<String, Integer> asyncTaskNameAndIndex = new HashMap<>();
        for (int i = 0; i < this.asyncTaskNames.size(); i++) {
            for (String asyncTaskName : this.asyncTaskNames.get(i)) {
                asyncTaskNameAndIndex.put(asyncTaskName, i + 1);
            }
        }
        double totalTimeSeconds = getTotalTimeSeconds();
        if (this.taskList != null) {
            for (TaskInfo taskInfo : this.taskList) {
                String taskName = taskInfo.taskName;
                if (executedTaskNames.contains(taskName)) {
                    continue;
                }
                List<TaskInfo> taskInfos = Lists.newArrayList(taskInfo);
                String nextGroupName = this.taskNameAndNextGroupName.get(taskName);
                double groupTaskTotalTimeSeconds = totalTimeSeconds;
                if (StringUtils.isNotBlank(nextGroupName)) {
                    taskInfos = this.taskGroupNameAndTasks.get(nextGroupName);
                    if (CollectionUtils.isNotEmpty(taskInfos)) {
                        long taskGroupTotalTimeNanos = taskInfos.stream()
                                .mapToLong(TaskInfo::getTimeNanos)
                                .reduce(0L, Long::sum);
                        String groupTotal = (timeUnit == TimeUnit.NANOSECONDS ?
                                nf.format(taskGroupTotalTimeNanos) :
                                nf.format(formatTime(taskGroupTotalTimeNanos, timeUnit)));
                        groupTaskTotalTimeSeconds = formatTime(taskGroupTotalTimeNanos, TimeUnit.SECONDS);
                        groupTotal = String.format("%-14s", groupTotal);
                        if (!fistGroup) {
                            sb.append(line);
                        }
                        sb.append(groupTotal);
                        sb.append(String.format("%-8s",
                                pf.format(groupTaskTotalTimeSeconds / totalTimeSeconds)));
                        sb.append("[GroupTask] ").append(nextGroupName).append("\n");
                        StringBuilder groupLineBuilder = new StringBuilder();
                        for (int i = 0; i < width; i++) {
                            groupLineBuilder.append(i % 2 == 0 ? '↓' : ' ');
                        }
                        sb.append(groupLineBuilder).append("\n");
                        fistGroup = false;
                    }
                }
                for (TaskInfo task : taskInfos) {
                    sb.append(String.format("%-14s", (timeUnit == TimeUnit.NANOSECONDS ?
                            nf.format(task.getTimeNanos()) : nf.format(task.getTime(timeUnit)))));
                    sb.append(String.format("%-8s",
                            pf.format(task.getTimeSeconds() / groupTaskTotalTimeSeconds)));
                    if (asyncTaskNameAndIndex.containsKey(task.taskName)) {
                        sb.append("[Async-").append(asyncTaskNameAndIndex.get(task.taskName)).append("] ");
                    }
                    sb.append(task.getTaskName()).append('\n');
                    executedTaskNames.add(task.taskName);
                }
            }
        } else {
            sb.append("No task info kept");
        }
        return sb.toString();
    }

    /**
     * 格式化时间
     *
     * @param time     时间
     * @param timeUnit 单位
     * @return 格式化后的时间
     * @since 7.250302.0
     */
    private double formatTime(long time, @NonNull TimeUnit timeUnit) {
        return (double) time / TimeUnit.NANOSECONDS.convert(1, timeUnit);
    }

    /**
     * 添加任务
     *
     * @param taskInfo 任务
     * @since 7.250302.0
     */
    private synchronized void addTaskInfo(@NonNull TaskInfo taskInfo) {
        addTaskInfo(Collections.singletonList(taskInfo));
    }

    /**
     * 添加任务
     *
     * @param taskInfos 任务
     * @since 7.250302.0
     */
    private synchronized void addTaskInfo(@NonNull List<TaskInfo> taskInfos) {
        if (this.taskList != null) {
            this.taskList.addAll(taskInfos);
            this.lastTaskInfo = taskInfos.get(taskInfos.size() - 1);
        }
        this.taskCount += taskInfos.size();
    }

    // ------------------------- 自定义方法结束 -------------------------

    /**
     * Construct a new {@code StopWatch}.
     * <p>Does not start any task.
     */
    public StopWatch() {
        this("");
    }

    /**
     * Construct a new {@code StopWatch} with the given id.
     * <p>The id is handy when we have output from multiple stop watches and need
     * to distinguish between them.
     * <p>Does not start any task.
     *
     * @param id identifier for this stop watch
     */
    public StopWatch(String id) {
        this.id = id;
    }


    /**
     * Configure whether the {@link TaskInfo} array is built over time.
     * <p>Set this to {@code false} when using a {@code StopWatch} for millions of
     * tasks; otherwise, the {@code TaskInfo} structure will consume excessive memory.
     * <p>Default is {@code true}.
     */
    public void setKeepTaskList(boolean keepTaskList) {
        this.taskList = (keepTaskList ? new ArrayList<>() : null);
    }


    /**
     * Start an unnamed task.
     * <p>The results are undefined if {@link #stop()} or timing methods are
     * called without invoking this method first.
     *
     * @see #start(String)
     * @see #stop()
     */
    public void start() throws IllegalStateException {
        start("");
    }

    /**
     * Start a named task.
     * <p>The results are undefined if {@link #stop()} or timing methods are
     * called without invoking this method first.
     *
     * @param taskName the name of the task to start
     * @see #start()
     * @see #stop()
     */
    public void start(String taskName) throws IllegalStateException {
        if (this.currentTaskName != null) {
            throw new IllegalStateException("Can't start StopWatch: it's already running");
        }
        this.currentTaskName = taskName;
        this.startTimeNanos = System.nanoTime();
    }

    /**
     * Stop the current task.
     * <p>The results are undefined if timing methods are called without invoking
     * at least one pair of {@code start()} / {@code stop()} methods.
     *
     * @see #start()
     * @see #start(String)
     */
    public void stop() throws IllegalStateException {
        if (this.currentTaskName == null) {
            throw new IllegalStateException("Can't stop StopWatch: it's not running");
        }
        long lastTime = System.nanoTime() - this.startTimeNanos;
        this.totalTimeNanos += lastTime;
        this.lastTaskInfo = new TaskInfo(this.currentTaskName, lastTime);
        if (this.taskList != null) {
            this.taskList.add(this.lastTaskInfo);
        }
        ++this.taskCount;
        this.currentTaskName = null;
    }

    /**
     * Determine whether this {@code StopWatch} is currently running.
     *
     * @see #currentTaskName()
     */
    public boolean isRunning() {
        return (this.currentTaskName != null);
    }

    /**
     * Get the name of the currently running task, if any.
     *
     * @see #isRunning()
     * @since 4.2.2
     */
    public @Nullable String currentTaskName() {
        return this.currentTaskName;
    }

    /**
     * Get the last task as a {@link TaskInfo} object.
     *
     * @throws IllegalStateException if no tasks have run yet
     * @since 6.1
     */
    public TaskInfo lastTaskInfo() throws IllegalStateException {
        Assert.state(this.lastTaskInfo != null, "No tasks run");
        return this.lastTaskInfo;
    }

    /**
     * Get an array of the data for tasks performed.
     *
     * @see #setKeepTaskList
     */
    public TaskInfo[] getTaskInfo() {
        if (this.taskList == null) {
            throw new UnsupportedOperationException("Task info is not being kept!");
        }
        return this.taskList.toArray(new TaskInfo[0]);
    }

    /**
     * Get the total time for all tasks in milliseconds.
     *
     * @see #getTotalTime(TimeUnit)
     */
    public long getTotalTimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(this.totalTimeNanos);
    }

    /**
     * Get the total time for all tasks in seconds.
     *
     * @see #getTotalTime(TimeUnit)
     */
    public double getTotalTimeSeconds() {
        return getTotalTime(TimeUnit.SECONDS);
    }

    /**
     * Get the total time for all tasks in the requested time unit
     * (with decimal points in nanosecond precision).
     *
     * @param timeUnit the unit to use
     * @see #getTotalTimeNanos()
     * @see #getTotalTimeMillis()
     * @see #getTotalTimeSeconds()
     * @since 6.1
     */
    public double getTotalTime(TimeUnit timeUnit) {
        return (double) this.totalTimeNanos / TimeUnit.NANOSECONDS.convert(1, timeUnit);
    }


    /**
     * Generate a table describing all tasks performed in seconds
     * (with decimal points in nanosecond precision).
     * <p>For custom reporting, call {@link #getTaskInfo()} and use the data directly.
     *
     * @see #prettyPrint(TimeUnit)
     * @see #getTotalTimeSeconds()
     * @see TaskInfo#getTimeSeconds()
     */
    public String prettyPrint() {
        return prettyPrint(TimeUnit.SECONDS);
    }

    /**
     * Generate a table describing all tasks performed in the requested time unit
     * (with decimal points in nanosecond precision).
     * <p>For custom reporting, call {@link #getTaskInfo()} and use the data directly.
     *
     * @param timeUnit the unit to use for rendering total time and task time
     * @see #prettyPrint()
     * @see #getTotalTime(TimeUnit)
     * @see TaskInfo#getTime(TimeUnit)
     * @since 6.1
     */
    public String prettyPrint(TimeUnit timeUnit) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(9);
        nf.setGroupingUsed(false);

        NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
        pf.setMinimumIntegerDigits(2);
        pf.setGroupingUsed(false);

        StringBuilder sb = new StringBuilder(128);
        sb.append("StopWatch '").append(getId()).append("': ");
        String total = (timeUnit == TimeUnit.NANOSECONDS ?
                nf.format(getTotalTimeNanos()) : nf.format(getTotalTime(timeUnit)));
        sb.append(total).append(" ").append(timeUnit.name().toLowerCase(Locale.ENGLISH));
        int width = Math.max(sb.length(), 40);
        sb.append("\n");

        if (this.taskList != null) {
            StringBuilder lineBuilder = new StringBuilder();
            for (int i = 0; i < width; i++) {
                lineBuilder.append('-');
            }
            String line = lineBuilder + "\n";
            String unitName = timeUnit.name();
            unitName = unitName.charAt(0) + unitName.substring(1).toLowerCase(Locale.ENGLISH);
            unitName = String.format("%-12s", unitName);
            sb.append(line);
            sb.append(unitName).append("  %       Task name\n");
            sb.append(line);

            int digits = total.indexOf('.');
            if (digits < 0) {
                digits = total.length();
            }
            nf.setMinimumIntegerDigits(digits);
            nf.setMaximumFractionDigits(10 - digits);

            for (TaskInfo task : this.taskList) {
                sb.append(String.format("%-14s", (timeUnit == TimeUnit.NANOSECONDS ?
                        nf.format(task.getTimeNanos()) : nf.format(task.getTime(timeUnit)))));
                sb.append(String.format("%-8s",
                        pf.format(task.getTimeSeconds() / getTotalTimeSeconds())));
                sb.append(task.getTaskName()).append('\n');
            }
        } else {
            sb.append("No task info kept");
        }

        return sb.toString();
    }

    /**
     * Get a short description of the total running time in seconds.
     *
     * @see #prettyPrint()
     * @see #prettyPrint(TimeUnit)
     */
    public String shortSummary() {
        return "StopWatch '" + getId() + "': " + getTotalTimeSeconds() + " seconds";
    }

    /**
     * Generate an informative string describing all tasks performed in seconds.
     *
     * @see #prettyPrint()
     * @see #prettyPrint(TimeUnit)
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(shortSummary());
        if (this.taskList != null) {
            for (TaskInfo task : this.taskList) {
                sb.append("; [").append(task.getTaskName()).append("] took ").append(task.getTimeSeconds()).append(" seconds");
                long percent = Math.round(100.0 * task.getTimeSeconds() / getTotalTimeSeconds());
                sb.append(" = ").append(percent).append('%');
            }
        } else {
            sb.append("; no task info kept");
        }
        return sb.toString();
    }


    /**
     * Nested class to hold data about one task executed within the {@code StopWatch}.
     */
    @Getter
    public static final class TaskInfo {

        private final String taskName;

        private final long timeNanos;

        TaskInfo(String taskName, long timeNanos) {
            this.taskName = taskName;
            this.timeNanos = timeNanos;
        }

        /**
         * Get the time this task took in milliseconds.
         *
         * @see #getTime(TimeUnit)
         */
        public long getTimeMillis() {
            return TimeUnit.NANOSECONDS.toMillis(this.timeNanos);
        }

        /**
         * Get the time this task took in seconds.
         *
         * @see #getTime(TimeUnit)
         */
        public double getTimeSeconds() {
            return getTime(TimeUnit.SECONDS);
        }

        /**
         * Get the time this task took in the requested time unit
         * (with decimal points in nanosecond precision).
         *
         * @param timeUnit the unit to use
         * @see #getTimeNanos()
         * @see #getTimeMillis()
         * @see #getTimeSeconds()
         * @since 6.1
         */
        public double getTime(TimeUnit timeUnit) {
            return (double) this.timeNanos / TimeUnit.NANOSECONDS.convert(1, timeUnit);
        }
    }

}