package com.example.spectestengine.engine;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class TestRunQueue {

    //Each url has its own queue
    private final Map<String, BlockingQueue<Runnable>> queues = new ConcurrentHashMap<>();

    //Each url queue has its own Virtual thread worker
    private final Map<String, Future<?>> dispatchers = new ConcurrentHashMap<>();

    //Virtual Threads for Dispatchers
    private final ExecutorService dispatcherExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public void submit(String url, Runnable task) {
        BlockingQueue<Runnable> queue = queues.computeIfAbsent(url, k -> new LinkedBlockingQueue<>());
        queue.add(task);
        dispatchers.computeIfAbsent(url, k -> dispatcherExecutor.submit(() -> runDispatcher(queue)));
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
