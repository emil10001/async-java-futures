package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;
import rx.Observable;
import rx.schedulers.Schedulers;

public class MultiRxJavaExample {

    public static void handleResponse(Content c) {
        SharedMethods.log("update result: " + c.asString());
    }

    public static void main(String[] args) {
        SharedMethods.log("main");
        HttpServer server = SharedMethods.server();

        long startTime = System.currentTimeMillis();

        Observable.just("1","2","3")
                .map(s -> SharedMethods.request(s))
                .subscribeOn(Schedulers.from(SharedMethods.requestService))
                .observeOn(Schedulers.io())
                .subscribe(c -> handleResponse(c));

        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime for observable: " + blockedTime + "ms");

        SharedMethods.teardown(server);

        SharedMethods.log("finish");
    }

}
