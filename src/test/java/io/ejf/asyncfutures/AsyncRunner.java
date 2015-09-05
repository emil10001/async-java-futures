package io.ejf.asyncfutures;

import org.apache.http.impl.bootstrap.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by ejf3 on 9/5/15.
 */
public class AsyncRunner {
    static HttpServer server;
    static final SharedMethods.Log log_ = new SharedMethods.Log(AsyncRunner.class);

    @BeforeClass
    public static void setup() {
        log_.log("setup");
        server = SharedMethods.server();
    }

    @Test
    public void blockingTest() {
        log_.log("blockingTest");
        BlockingExample.doBlockingRequest();
    }

    @Test
    public void multiBlockingTest() {
        log_.log("multiBlockingTest");
        String result = MultiBlockingExample.doBlockingRequest("0");
        log_.log("request content: " + result);

        result = MultiBlockingExample.doBlockingRequest(result);
        log_.log("request content: " + result);
    }

    @Test
    public void callbackTest() {
        log_.log("callbackTest");
        CallbackExample.ContentCallback content = new CallbackExample.ContentCallback();
        CallbackExample.doCallbackRequest(content);
    }

    @Test
    public void observerTest() {
        log_.log("observerTest");
        ObserverExample.ObservableContent content = new ObserverExample.ObservableContent();
        content.addObserver(new ObserverExample.ContentObserver());
        ObserverExample.doObserverRequest(content);
    }

    @Test
    public void multiObserverTest() {
        log_.log("multiObserverTest");
        MultiObserverExample.ObservableContent content = new MultiObserverExample.ObservableContent();
        content.addObserver(new MultiObserverExample.ContentObserver(3));
        MultiObserverExample.doObserverRequest(content);
        MultiObserverExample.doObserverRequest(content);
        MultiObserverExample.doObserverRequest(content);
    }

    @Test
    public void rxTest() {
        log_.log("rxTest");
        Observable.just(0)
                .map(i -> SharedMethods.request())
                .subscribeOn(Schedulers.from(SharedMethods.requestService))
                .observeOn(Schedulers.io())
                .subscribe(c -> RxJavaExample.handleResponse(c));
    }

    @Test
    public void multiRxTest() {
        log_.log("multiRxTest");
        Observable.just("1", "2", "3")
                .map(s -> SharedMethods.request(s))
                .subscribeOn(Schedulers.from(SharedMethods.requestService))
                .observeOn(Schedulers.io())
                .subscribe(c -> MultiRxJavaExample.handleResponse(c));
    }

    @Test
    public void wrapperTest() {
        log_.log("wrapperTest");
        WrapperExample.doCallbackRequest();
    }

    @AfterClass
    public static void tearDown() {
        log_.log("tearDown");
        SharedMethods.teardown(server);
        log_.log("tearDown finished");

    }
}
