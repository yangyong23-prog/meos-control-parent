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
 *     <li>每次发送成功后，若 spool 文件存在则整体重放，重放失败的部分写回 spool。</li>
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
    void flush(List<T> batch) {
        if (send(batch)) {
            OptMetrics.add(name + ".sent", batch.size());
            replaySpool();
        } else {
            spool(batch);
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
        if (spoolFile == null) {
            OptMetrics.addAlways(name + ".spoolDrop", batch.size());
            return;
        }
        try {
            Files.createDirectories(spoolFile.toAbsolutePath().getParent());
            if (Files.exists(spoolFile) && Files.size(spoolFile) >= maxSpoolBytes) {
                OptMetrics.addAlways(name + ".spoolDrop", batch.size());
                return;
            }
            try (BufferedWriter w = Files.newBufferedWriter(spoolFile, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                for (T item : batch) {
                    w.write(mapper.writeValueAsString(item));
                    w.newLine();
                }
            }
        } catch (IOException e) {
            log.error("[opt] {} 写本地重放文件失败，丢弃 {} 条", name, batch.size(), e);
            OptMetrics.addAlways(name + ".spoolDrop", batch.size());
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
        int replayed = 0;
        List<T> failed = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(replaying, StandardCharsets.UTF_8)) {
            List<T> batch = new ArrayList<>(batchSize);
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                batch.add(mapper.readValue(line, type));
                if (batch.size() >= batchSize) {
                    replayed += replayBatch(batch, failed);
                    batch = new ArrayList<>(batchSize);
                }
            }
            if (!batch.isEmpty()) {
                replayed += replayBatch(batch, failed);
            }
        } catch (IOException e) {
            log.error("[opt] {} 读取重放文件失败", name, e);
        }
        try {
            Files.deleteIfExists(replaying);
        } catch (IOException e) {
            log.warn("[opt] {} 删除重放文件失败: {}", name, e.toString());
        }
        if (!failed.isEmpty()) {
            spool(failed);
        }
        if (replayed > 0) {
            OptMetrics.add(name + ".replayed", replayed);
            log.info("[opt] {} 已重放 {} 条，仍失败 {} 条", name, replayed, failed.size());
        }
    }

    private int replayBatch(List<T> batch, List<T> failed) {
        // 一旦失败，后续批次也不再尝试，原样写回，保持顺序
        if (!failed.isEmpty() || !send(batch)) {
            failed.addAll(batch);
            return 0;
        }
        return batch.size();
    }

    public void stop() {
        running = false;
        worker.interrupt();
    }
}
