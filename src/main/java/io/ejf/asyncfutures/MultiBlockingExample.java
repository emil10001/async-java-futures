package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;

import java.util.concurrent.Future;

public class MultiBlockingExample {
    static final SharedMethods.Log log_ = new SharedMethods.Log(MultiBlockingExample.class);

    public static String doBlockingRequest(String appended) {
        log_.log("doBlockingRequest");

        long startTime = System.currentTimeMillis();
        Future<Content> futureA = SharedMethods.requestService.submit(() -> SharedMethods.request());
        Future<Content> futureB = SharedMethods.requestService.submit(() -> SharedMethods.request());

        String result = "";

        try {
            result = futureA.get().asString() + "/" + appended + "/" + futureB.get().asString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long blockedTime = (System.currentTimeMillis() - startTime);
        log_.log("blockedTime: " + blockedTime + "ms");

        return result;
    }

    public static void main(String[] args) {
        log_.log("main");
        HttpServer server = SharedMethods.server();
        String result = MultiBlockingExample.doBlockingRequest("0");
        log_.log("request content: " + result);

        result = MultiBlockingExample.doBlockingRequest(result);
        log_.log("request content: " + result);

        SharedMethods.teardown(server);

        log_.log("finish");
    }
}
