package com.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class AsyncLogger {
    private final Thread writerThread;

    public AsyncLogger(String filePath, BlockingQueue<String> logQueue) {
        writerThread = new Thread(() -> {
            System.out.println("Logger thread started.");
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true), true)) { // autoFlush = true
                while (true) {
                    String log = logQueue.poll(5, TimeUnit.SECONDS);
                    if (log == null) continue;
                    if (log.equals("__EOF__")) break;
                    writer.println(log);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        writerThread.start();
    }

    public void shutdown(BlockingQueue<String> logQueue) {
        logQueue.offer("__EOF__");
    }
}
