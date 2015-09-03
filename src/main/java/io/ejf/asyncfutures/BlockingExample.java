package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;

import java.util.concurrent.*;

public class BlockingExample {
    public static void doBlockingRequest() {
        SharedMethods.log("doBlockingRequest");

        long startTime = System.currentTimeMillis();
        Future<Content> future = SharedMethods.requestService.submit(() -> SharedMethods.request());

        while (!future.isDone()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime: " + blockedTime + "ms");

        try {
            Content content = future.get();
            SharedMethods.log("request content: " + content.asString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SharedMethods.log("main");
        HttpServer server = SharedMethods.server();
        BlockingExample.doBlockingRequest();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        server.shutdown(0L, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SharedMethods.serverService.shutdownNow();
        SharedMethods.requestService.shutdownNow();

        while (!SharedMethods.serverService.isShutdown()
                || !SharedMethods.requestService.isShutdown()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SharedMethods.log("finish");
    }
}
