package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;

import java.util.concurrent.locks.ReentrantLock;

public class WrapperExample {
    protected static ReentrantLock lock = new ReentrantLock();
    static final SharedMethods.Log log_ = new SharedMethods.Log(WrapperExample.class);

    private static class WrapperClass {
        static final WrapperClass INSTANCE = new WrapperClass();

        public void setUpdateContent(Content content) {
            log_.log("update result" + content.asString());
            lock.unlock();
        }
    }

    public static void doCallbackRequest() {
        log_.log("doCallbackRequest");

        long startTime = System.currentTimeMillis();
        SharedMethods.requestService.submit(() ->
            WrapperClass.INSTANCE.setUpdateContent(SharedMethods.request()));

        long blockedTime = (System.currentTimeMillis() - startTime);
        log_.log("blockedTime: " + blockedTime + "ms");
    }

    public static void main(String[] args) {
        log_.log("main");
        HttpServer server = SharedMethods.server();
        WrapperExample.doCallbackRequest();

        long startTime = System.currentTimeMillis();
        lock.lock();
        long blockedTime = (System.currentTimeMillis() - startTime);
        log_.log("blockedTime for lock: " + blockedTime + "ms");

        SharedMethods.teardown(server);

        log_.log("finish");
    }

}
