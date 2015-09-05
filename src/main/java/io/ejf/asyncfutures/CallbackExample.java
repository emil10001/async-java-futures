package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;

import java.util.concurrent.locks.ReentrantLock;

public class CallbackExample {
    protected static ReentrantLock lock = new ReentrantLock();

    protected static class ContentCallback implements Runnable {
        private Content content;

        public void setContent(Content c) {
            content = c;
        }

        @Override
        public void run() {
            SharedMethods.log("update result" + content.asString());
            lock.unlock();
        }
    }

    public static void doCallbackRequest(ContentCallback callback) {
        SharedMethods.log("doCallbackRequest");

        long startTime = System.currentTimeMillis();
        SharedMethods.requestService.submit(() -> {
            callback.setContent(SharedMethods.request());
            callback.run();
        });

        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime: " + blockedTime + "ms");
    }

    public static void main(String[] args) {
        SharedMethods.log("main");
        HttpServer server = SharedMethods.server();
        ContentCallback content = new ContentCallback();
        CallbackExample.doCallbackRequest(content);

        long startTime = System.currentTimeMillis();
        lock.lock();
        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime for lock: " + blockedTime + "ms");

        SharedMethods.teardown(server);

        SharedMethods.log("finish");
    }

}
