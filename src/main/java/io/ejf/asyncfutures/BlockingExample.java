package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;

import java.util.concurrent.*;

public class BlockingExample {

    public static void doBlockingRequest() {
        SharedMethods.log("doBlockingRequest");

        long startTime = System.currentTimeMillis();
        Future<Content> future = SharedMethods.requestService.submit(() -> SharedMethods.request());

        // future.isDone() is a non-blocking call, if you didn't need the value right away, you can periodically
        // check it to see when the task is finished executing, and the object inside future is ready
        SharedMethods.log("future.isDone? " + future.isDone());

        try {
            Content content = future.get();
            SharedMethods.log("request content: " + content.asString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime: " + blockedTime + "ms");
    }

    public static void main(String[] args) {
        SharedMethods.log("main");
        HttpServer server = SharedMethods.server();
        BlockingExample.doBlockingRequest();

        SharedMethods.teardown(server);

        SharedMethods.log("finish");
    }
}
