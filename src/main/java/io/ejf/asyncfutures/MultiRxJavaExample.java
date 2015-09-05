package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;
import rx.Observable;
import rx.schedulers.Schedulers;

public class MultiRxJavaExample {
    static final SharedMethods.Log log_ = new SharedMethods.Log(MultiRxJavaExample.class);

    public static void handleResponse(Content c) {
        log_.log("update result: " + c.asString());
    }

    public static void main(String[] args) {
        log_.log("main");
        HttpServer server = SharedMethods.server();

        long startTime = System.currentTimeMillis();

        Observable.just("1", "2", "3")
                .map(s -> SharedMethods.request(s))
                .subscribeOn(Schedulers.from(SharedMethods.requestService))
                .observeOn(Schedulers.io())
                .subscribe(c -> handleResponse(c));

        long blockedTime = (System.currentTimeMillis() - startTime);
        log_.log("blockedTime for observable: " + blockedTime + "ms");

        SharedMethods.teardown(server);

        log_.log("finish");
    }

}
