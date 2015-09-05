package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;

import java.util.concurrent.locks.ReentrantLock;

public class CallbackExample {
    protected static ReentrantLock lock = new ReentrantLock();
    static final SharedMethods.Log log_ = new SharedMethods.Log(CallbackExample.class);

    protected static class ContentCallback implements Runnable {
        private Content content;

        public void setContent(Content c) {
            content = c;
        }

        @Override
        public void run() {
            log_.log("update result" + content.asString());
            lock.unlock();
        }
    }

    public static void doCallbackRequest(ContentCallback callback) {
        log_.log("doCallbackRequest");

        long startTime = System.currentTimeMillis();
        SharedMethods.requestService.submit(() -> {
            callback.setContent(SharedMethods.request());
            callback.run();
        });

        long blockedTime = (System.currentTimeMillis() - startTime);
        log_.log("blockedTime: " + blockedTime + "ms");
    }

    public static void main(String[] args) {
        log_.log("main");
        HttpServer server = SharedMethods.server();
        ContentCallback content = new ContentCallback();
        CallbackExample.doCallbackRequest(content);

        long startTime = System.currentTimeMillis();
        lock.lock();
        long blockedTime = (System.currentTimeMillis() - startTime);
        log_.log("blockedTime for lock: " + blockedTime + "ms");

        SharedMethods.teardown(server);

        log_.log("finish");
    }

}
