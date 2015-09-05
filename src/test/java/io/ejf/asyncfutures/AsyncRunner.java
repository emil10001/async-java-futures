package io.ejf.asyncfutures;

import org.apache.http.impl.bootstrap.HttpServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by ejf3 on 9/5/15.
 */
public class AsyncRunner {
    static HttpServer server;

    @BeforeClass
    public static void setup() {
        SharedMethods.log("setup");
        server = SharedMethods.server();
    }

    @Before
    public void before(){
        System.out.println(" ============================= ");
    }

    @Test
    public void blockingTest() {
        SharedMethods.log("blockingTest");
        BlockingExample.doBlockingRequest();
    }

    @Test
    public void multiBlockingTest() {
        SharedMethods.log("multiBlockingTest");
        String result = MultiBlockingExample.doBlockingRequest("0");
        SharedMethods.log("request content: " + result);

        result = MultiBlockingExample.doBlockingRequest(result);
        SharedMethods.log("request content: " + result);
    }

    @Test
    public void callbackTest() {
        SharedMethods.log("callbackTest");
        CallbackExample.ContentCallback content = new CallbackExample.ContentCallback();
        CallbackExample.doCallbackRequest(content);

        long startTime = System.currentTimeMillis();
        CallbackExample.lock.lock();
        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime for lock: " + blockedTime + "ms");
    }

    @Test
    public void observerTest() {
        SharedMethods.log("observerTest");
        ObserverExample.ObservableContent content = new ObserverExample.ObservableContent();
        content.addObserver(new ObserverExample.ContentObserver());
        ObserverExample.doObserverRequest(content);

        long startTime = System.currentTimeMillis();
        ObserverExample.lock.lock();
        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime for lock: " + blockedTime + "ms");
    }

    @Test
    public void multiObserverTest() {
        SharedMethods.log("multiObserverTest");
        MultiObserverExample.ObservableContent content = new MultiObserverExample.ObservableContent();
        content.addObserver(new MultiObserverExample.ContentObserver(3));
        MultiObserverExample.doObserverRequest(content);
        MultiObserverExample.doObserverRequest(content);
        MultiObserverExample.doObserverRequest(content);

        long startTime = System.currentTimeMillis();
        MultiObserverExample.lock.lock();
        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime for lock: " + blockedTime + "ms");
    }

    @Test
    public void rxTest() {
        SharedMethods.log("rxTest");
        long startTime = System.currentTimeMillis();

        Observable.just(0)
                .map(i -> SharedMethods.request())
                .subscribeOn(Schedulers.from(SharedMethods.requestService))
                .observeOn(Schedulers.io())
                .subscribe(c -> RxJavaExample.handleResponse(c));

        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime for lock: " + blockedTime + "ms");
    }

    @Test
    public void multiRxTest() {
        SharedMethods.log("multiRxTest");
        long startTime = System.currentTimeMillis();

        Observable.just("1", "2", "3")
                .map(s -> SharedMethods.request(s))
                .subscribeOn(Schedulers.from(SharedMethods.requestService))
                .observeOn(Schedulers.io())
                .subscribe(c -> MultiRxJavaExample.handleResponse(c));

        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime for observable: " + blockedTime + "ms");
    }

    @Test
    public void wrapperTest() {
        SharedMethods.log("wrapperTest");
        WrapperExample.doCallbackRequest();

        long startTime = System.currentTimeMillis();
        WrapperExample.lock.lock();
        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime for lock: " + blockedTime + "ms");
    }

    @AfterClass
    public static void tearDown() {
        SharedMethods.log("tearDown");
        SharedMethods.teardown(server);
        SharedMethods.log("tearDown finished");

    }
}
