package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * B1：有界本地队列 + 单个后台线程合批上传，失败落本地文件，恢复后重放。
 *
 * <ul>
 *     <li>{@link #offer} 不阻塞：队列满时丢弃并计数 {@code <name>.drop}。</li>
 *     <li>后台线程每次最多取 {@code batchSize} 条，按入队顺序调用 sender；sender 抛异常视为失败。</li>
 *     <li>失败的批次按 JSON 行追加到 spool 文件（超过 maxSpoolBytes 时丢弃并计数 {@code <name>.spoolDrop}）。</li>
 *     <li>spool 有积压时，新批次先追加到 spool 末尾，再从头按序重放，保证旧消息一定先于新消息到达；
 *     重放失败的部分原样写回 spool。</li>
 *     <li>重放中某批失败时逐条重试：某条失败而紧随其后的一条成功，判定前者为毒消息，移入 {@code <spool>.dead}
 *     并计数 {@code <name>.dead}，不再卡住后续重放；连续两条失败视为下游不可用，停止本轮。</li>
 * </ul>
 * 单线程发送保证同一条日志的“先删后增”两条消息顺序不变。
 *
 * @param <T> 消息类型，需能被 Jackson 序列化/反序列化
 */
@Slf4j
public class OptBatchUploader<T> {

    private final String name;
    private final Class<T> type;
    private final Consumer<List<T>> sender;
    private final BlockingQueue<T> queue;
    private final int batchSize;
    private final Path spoolFile;
    private final long maxSpoolBytes;
    private final ObjectMapper mapper;
    private final Thread worker;
    private volatile boolean running = true;

    public OptBatchUploader(String name, Class<T> type, Consumer<List<T>> sender, int capacity, int batchSize,
                            Path spoolFile, long maxSpoolBytes, ObjectMapper mapper) {
        this.name = name;
        this.type = type;
        this.sender = sender;
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.batchSize = batchSize;
        this.spoolFile = spoolFile;
        this.maxSpoolBytes = maxSpoolBytes;
        this.mapper = mapper;
        this.worker = new Thread(this::loop, "opt-uploader-" + name);
        this.worker.setDaemon(true);
    }

    public OptBatchUploader<T> start() {
        worker.start();
        return this;
    }

    /**
     * 入队，不阻塞。多条消息要么全部入队，要么在队列放不下的位置开始丢弃。
     *
     * @return 全部入队返回 true
     */
    public boolean offer(Collection<T> items) {
        boolean all = true;
        for (T item : items) {
            if (!queue.offer(item)) {
                OptMetrics.incAlways(name + ".drop");
                all = false;
            }
        }
        return all;
    }

    public int queued() {
        return queue.size();
    }

    private void loop() {
        while (running) {
            try {
                T first = queue.poll(1, TimeUnit.SECONDS);
                if (first == null) {
                    continue;
                }
                List<T> batch = new ArrayList<>(batchSize);
                batch.add(first);
                queue.drainTo(batch, batchSize - 1);
                flush(batch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Throwable t) {
                log.error("[opt] {} 上传线程异常", name, t);
            }
        }
    }

    /**
     * 发送一批；成功后尝试重放 spool。包可见，便于单测同步驱动。
     */
    synchronized void flush(List<T> batch) {
        if (hasSpool()) {
            // 有积压时新批次排在积压之后，避免新消息先于旧消息到达
            spool(batch);
            replaySpool();
            return;
        }
        if (send(batch)) {
            OptMetrics.add(name + ".sent", batch.size());
        } else {
            spool(batch);
        }
    }

    private boolean hasSpool() {
        try {
            return spoolFile != null && Files.exists(spoolFile) && Files.size(spoolFile) > 0;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean send(List<T> batch) {
        try {
            sender.accept(batch);
            return true;
        } catch (Throwable t) {
            OptMetrics.incAlways(name + ".fail");
            log.warn("[opt] {} 上传失败 {} 条，写入本地文件待重放: {}", name, batch.size(), t.toString());
            return false;
        }
    }

    synchronized void spool(List<T> batch) {
        appendTo(spoolFile, batch, true);
    }

    private void appendTo(Path file, List<T> batch, boolean limited) {
        String drop = limited ? ".spoolDrop" : ".deadDrop";
        if (file == null) {
            OptMetrics.addAlways(name + ".spoolDrop", batch.size());
            return;
        }
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            if (Files.exists(file) && Files.size(file) >= maxSpoolBytes) {
                OptMetrics.addAlways(name + drop, batch.size());
                return;
            }
            try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                for (T item : batch) {
                    w.write(mapper.writeValueAsString(item));
                    w.newLine();
                }
            }
        } catch (IOException e) {
            log.error("[opt] {} 写本地文件 {} 失败，丢弃 {} 条", name, file, batch.size(), e);
            OptMetrics.addAlways(name + drop, batch.size());
        }
    }

    synchronized void replaySpool() {
        if (spoolFile == null || !Files.exists(spoolFile)) {
            return;
        }
        Path replaying = spoolFile.resolveSibling(spoolFile.getFileName() + ".replaying");
        try {
            Files.move(spoolFile, replaying, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("[opt] {} 无法开始重放: {}", name, e.toString());
            return;
        }
        Round round = new Round();
        try (BufferedReader r = Files.newBufferedReader(replaying, StandardCharsets.UTF_8)) {
            List<T> batch = new ArrayList<>(batchSize);
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                T item;
                try {
                    item = mapper.readValue(line, type);
                } catch (IOException e) {
                    // 无法解析的行同样按毒消息处理，不能让它中断整轮重放
                    deadLine(line);
                    continue;
                }
                batch.add(item);
                if (batch.size() >= batchSize) {
                    replayBatch(batch, round);
                    batch = new ArrayList<>(batchSize);
                }
            }
            if (!batch.isEmpty()) {
                replayBatch(batch, round);
            }
        } catch (IOException e) {
            log.error("[opt] {} 读取重放文件失败", name, e);
        }
        try {
            Files.deleteIfExists(replaying);
        } catch (IOException e) {
            log.warn("[opt] {} 删除重放文件失败: {}", name, e.toString());
        }
        // 末尾单条失败无法判定是否毒消息，连同未发送部分一起写回，下一轮再判
        round.failed.addAll(0, round.suspect);
        if (!round.failed.isEmpty()) {
            spool(round.failed);
        }
        if (round.replayed > 0) {
            OptMetrics.add(name + ".replayed", round.replayed);
            log.info("[opt] {} 已重放 {} 条，仍失败 {} 条", name, round.replayed, round.failed.size());
        }
    }

    /** 一轮重放的状态 */
    private final class Round {
        int replayed;
        /** 单独发送失败、尚待后一条判定的消息（至多 1 条） */
        final List<T> suspect = new ArrayList<>(1);
        /** 判定下游不可用后剩余的消息，按原顺序写回 */
        final List<T> failed = new ArrayList<>();
    }

    private void replayBatch(List<T> batch, Round round) {
        // 一旦判定下游不可用，后续批次不再尝试，原样写回，保持顺序
        if (!round.failed.isEmpty()) {
            round.failed.addAll(batch);
            return;
        }
        if (round.suspect.isEmpty() && send(batch)) {
            round.replayed += batch.size();
            return;
        }
        replayOneByOne(batch, round);
    }

    /**
     * 逐条重试：失败后紧跟一条成功，前面失败的就是毒消息；连续两条失败视为下游不可用，停止本轮。
     */
    private void replayOneByOne(List<T> batch, Round round) {
        for (int i = 0; i < batch.size(); i++) {
            T item = batch.get(i);
            if (send(Collections.singletonList(item))) {
                round.replayed++;
                if (!round.suspect.isEmpty()) {
                    dead(round.suspect);
                    round.suspect.clear();
                }
            } else if (round.suspect.isEmpty()) {
                round.suspect.add(item);
            } else {
                round.failed.addAll(round.suspect);
                round.suspect.clear();
                round.failed.addAll(batch.subList(i, batch.size()));
                return;
            }
        }
    }

    private void deadLine(String line) {
        OptMetrics.incAlways(name + ".dead");
        log.error("[opt] {} 重放文件中有无法解析的行，移入 dead 文件", name);
        try (BufferedWriter w = Files.newBufferedWriter(deadFile(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(line);
            w.newLine();
        } catch (IOException e) {
            OptMetrics.incAlways(name + ".deadDrop");
        }
    }

    private Path deadFile() {
        return spoolFile.resolveSibling(spoolFile.getFileName() + ".dead");
    }

    private void dead(List<T> items) {
        OptMetrics.addAlways(name + ".dead", items.size());
        log.error("[opt] {} {} 条消息单独发送失败而后续成功，判定为毒消息，移入 dead 文件", name, items.size());
        appendTo(deadFile(), items, false);
    }

    public void stop() {
        running = false;
        worker.interrupt();
    }
}
