package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;

import java.util.concurrent.*;

public class BlockingExample {
    static final SharedMethods.Log log_ = new SharedMethods.Log(BlockingExample.class);

    public static void doBlockingRequest() {
        log_.log("doBlockingRequest");

        long startTime = System.currentTimeMillis();
        Future<Content> future = SharedMethods.requestService.submit(() -> SharedMethods.request());

        // future.isDone() is a non-blocking call, if you didn't need the value right away, you can periodically
        // check it to see when the task is finished executing, and the object inside future is ready
        log_.log("future.isDone? " + future.isDone());

        try {
            Content content = future.get();
            log_.log("request content: " + content.asString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        long blockedTime = (System.currentTimeMillis() - startTime);
        log_.log("blockedTime: " + blockedTime + "ms");
    }

    public static void main(String[] args) {
        log_.log("main");
        HttpServer server = SharedMethods.server();
        BlockingExample.doBlockingRequest();

        SharedMethods.teardown(server);

        log_.log("finish");
    }
}
