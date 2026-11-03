package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class OptBatchUploaderTest {

    public static class Msg {
        public String key;

        public Msg() {
        }

        Msg(String key) {
            this.key = key;
        }
    }

    @TempDir
    Path dir;

    @Test
    void offerDoesNotBlockAndDropsWhenFull() {
        OptBatchUploader<Msg> uploader = new OptBatchUploader<>("t", Msg.class, b -> { }, 2, 10,
                dir.resolve("s.jsonl"), 1024, new ObjectMapper());
        Assertions.assertTrue(uploader.offer(Arrays.asList(new Msg("a"), new Msg("b"))));
        Assertions.assertFalse(uploader.offer(Collections.singletonList(new Msg("c"))));
        Assertions.assertEquals(2, uploader.queued());
    }

    @Test
    void failedBatchIsSpooledAndReplayedInOrderAfterRecovery() throws Exception {
        AtomicBoolean down = new AtomicBoolean(true);
        List<String> sent = new CopyOnWriteArrayList<>();
        Path spool = dir.resolve("sub/s.jsonl");
        OptBatchUploader<Msg> uploader = new OptBatchUploader<>("t", Msg.class, batch -> {
            if (down.get()) {
                throw new IllegalStateException("datatransfer 不可达");
            }
            batch.forEach(m -> sent.add(m.key));
        }, 100, 2, spool, 1024 * 1024, new ObjectMapper());

        uploader.flush(Arrays.asList(new Msg("del-1"), new Msg("add-1")));
        uploader.flush(Collections.singletonList(new Msg("add-2")));
        Assertions.assertTrue(Files.exists(spool));
        Assertions.assertEquals(3, Files.readAllLines(spool).size());
        Assertions.assertTrue(sent.isEmpty());

        down.set(false);
        uploader.flush(Collections.singletonList(new Msg("add-3")));
        // 有积压时新消息排在积压之后，旧消息一定先到
        Assertions.assertEquals(Arrays.asList("del-1", "add-1", "add-2", "add-3"), sent);
        Assertions.assertFalse(Files.exists(spool));
    }

    @Test
    void spoolStopsGrowingAtLimit() throws Exception {
        Path spool = dir.resolve("s.jsonl");
        OptBatchUploader<Msg> uploader = new OptBatchUploader<>("t", Msg.class, b -> {
            throw new IllegalStateException("down");
        }, 100, 10, spool, 10, new ObjectMapper());
        uploader.flush(Collections.singletonList(new Msg("aaaaaaaaaaaa")));
        long size = Files.size(spool);
        uploader.flush(Collections.singletonList(new Msg("b")));
        Assertions.assertEquals(size, Files.size(spool));
    }

    @Test
    void backgroundWorkerSendsQueuedItems() throws Exception {
        List<String> sent = new CopyOnWriteArrayList<>();
        OptBatchUploader<Msg> uploader = new OptBatchUploader<>("t", Msg.class,
                b -> b.forEach(m -> sent.add(m.key)), 100, 10, null, 0, new ObjectMapper()).start();
        List<Msg> items = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            items.add(new Msg(String.valueOf(i)));
        }
        uploader.offer(items);
        long deadline = System.currentTimeMillis() + 5000;
        while (sent.size() < 25 && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        uploader.stop();
        Assertions.assertEquals(25, sent.size());
        Assertions.assertEquals("0", sent.get(0));
        Assertions.assertEquals("24", sent.get(24));
    }

    @Test
    void poisonMessageIsMovedToDeadFileAndDoesNotBlockReplay() throws Exception {
        AtomicBoolean down = new AtomicBoolean(true);
        List<String> sent = new CopyOnWriteArrayList<>();
        Path spool = dir.resolve("s.jsonl");
        OptBatchUploader<Msg> uploader = new OptBatchUploader<>("t", Msg.class, batch -> {
            if (down.get() || batch.stream().anyMatch(m -> "poison".equals(m.key))) {
                throw new IllegalStateException("rejected");
            }
            batch.forEach(m -> sent.add(m.key));
        }, 100, 10, spool, 1024 * 1024, new ObjectMapper());

        uploader.flush(Arrays.asList(new Msg("a"), new Msg("poison"), new Msg("b")));
        down.set(false);
        uploader.flush(Collections.singletonList(new Msg("c")));

        Assertions.assertEquals(Arrays.asList("a", "b", "c"), sent);
        Assertions.assertFalse(Files.exists(spool));
        List<String> dead = Files.readAllLines(dir.resolve("s.jsonl.dead"));
        Assertions.assertEquals(1, dead.size());
        Assertions.assertTrue(dead.get(0).contains("poison"));
    }

    @Test
    void poisonAtEndOfBacklogIsJudgedWhenNextMessageArrives() throws Exception {
        List<String> sent = new CopyOnWriteArrayList<>();
        Path spool = dir.resolve("s.jsonl");
        OptBatchUploader<Msg> uploader = new OptBatchUploader<>("t", Msg.class, batch -> {
            if (batch.stream().anyMatch(m -> "poison".equals(m.key))) {
                throw new IllegalStateException("rejected");
            }
            batch.forEach(m -> sent.add(m.key));
        }, 100, 10, spool, 1024 * 1024, new ObjectMapper());

        uploader.flush(Collections.singletonList(new Msg("poison")));
        Assertions.assertTrue(Files.exists(spool), "单条失败无法判定，先留在积压里");
        uploader.flush(Collections.singletonList(new Msg("next")));
        Assertions.assertEquals(Collections.singletonList("next"), sent);
        Assertions.assertFalse(Files.exists(spool));
        Assertions.assertEquals(1, Files.readAllLines(dir.resolve("s.jsonl.dead")).size());
    }

    @Test
    void outageKeepsEverythingInOrderAndDeadLettersNothing() throws Exception {
        List<String> sent = new CopyOnWriteArrayList<>();
        AtomicBoolean down = new AtomicBoolean(true);
        Path spool = dir.resolve("s.jsonl");
        OptBatchUploader<Msg> uploader = new OptBatchUploader<>("t", Msg.class, batch -> {
            if (down.get()) {
                throw new IllegalStateException("down");
            }
            batch.forEach(m -> sent.add(m.key));
        }, 100, 2, spool, 1024 * 1024, new ObjectMapper());
        for (int i = 0; i < 5; i++) {
            uploader.flush(Arrays.asList(new Msg("d" + i), new Msg("a" + i)));
        }
        Assertions.assertEquals(10, Files.readAllLines(spool).size());
        Assertions.assertFalse(Files.exists(dir.resolve("s.jsonl.dead")));
        down.set(false);
        uploader.flush(Collections.singletonList(new Msg("x")));
        Assertions.assertEquals(Arrays.asList("d0", "a0", "d1", "a1", "d2", "a2", "d3", "a3", "d4", "a4", "x"), sent);
    }

    @Test
    void unparseableSpoolLineIsMovedToDeadFile() throws Exception {
        List<String> sent = new CopyOnWriteArrayList<>();
        Path spool = dir.resolve("s.jsonl");
        Files.write(spool, Arrays.asList("{\"key\":\"a\"}", "not-json", "{\"key\":\"b\"}"));
        OptBatchUploader<Msg> uploader = new OptBatchUploader<>("t", Msg.class,
                b -> b.forEach(m -> sent.add(m.key)), 100, 10, spool, 1024 * 1024, new ObjectMapper());
        uploader.flush(Collections.singletonList(new Msg("c")));
        Assertions.assertEquals(Arrays.asList("a", "b", "c"), sent);
        Assertions.assertEquals(Collections.singletonList("not-json"), Files.readAllLines(dir.resolve("s.jsonl.dead")));
    }
}
