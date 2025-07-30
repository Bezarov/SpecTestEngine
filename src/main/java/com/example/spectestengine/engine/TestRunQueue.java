package com.example.spectestengine.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class TestRunQueue {
    private static final int MAX_QUEUE_SIZE = 1000;

    private final Map<String, BlockingQueue<Runnable>> asynchronousQueue = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> dispatchers = new ConcurrentHashMap<>();
    private final ExecutorService dispatcherExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public void submit(String url, Runnable task) {
        BlockingQueue<Runnable> synchronizedQueue = asynchronousQueue.computeIfAbsent(url, urlAsKey ->
                new LinkedBlockingQueue<>(MAX_QUEUE_SIZE));

        if (synchronizedQueue.offer(task)) {
            synchronizedQueue.add(task);
            dispatchers.computeIfAbsent(url, urlAsKey -> dispatcherExecutor.submit(() ->
                    runDispatcher(synchronizedQueue)));
        } else {
            log.warn("Queue is full for URL: '{}' , queue size is '{}'", url, synchronizedQueue.size());
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Queue is full for URL: '%s' try again later".formatted(url));
        }
    }

    private void runDispatcher(BlockingQueue<Runnable> queue) {
        try {
            while (true) {
                Runnable task = queue.take();
                task.run();
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
