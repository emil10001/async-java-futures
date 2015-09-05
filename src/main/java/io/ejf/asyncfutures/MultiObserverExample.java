package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MultiObserverExample {
    private static ReentrantLock lock = new ReentrantLock();
    private static final ConcurrentLinkedQueue<String> resultsQueue = new ConcurrentLinkedQueue<>();

    private static class ContentObserver implements Observer {
        private final AtomicInteger expectedResults;

        private ContentObserver(int expectedResults) {
            this.expectedResults = new AtomicInteger(expectedResults);
        }

        @Override
        public void update(Observable o, Object arg) {
            if (!(o instanceof ObservableContent))
                return;

            resultsQueue.add(String.valueOf(expectedResults.get()) + " "
                    + ((ObservableContent) o).getContent().asString());

            if (0 == this.expectedResults.decrementAndGet())
                finished();
        }

    }

    private static void finished() {
        SharedMethods.log("finished");
        for (String result : resultsQueue)
            SharedMethods.log("update result: " + result);

        lock.unlock();
    }

    private static class ObservableContent extends Observable {
        private Content content = null;

        public ObservableContent() {
        }

        public void setContent(Content content) {
            this.content = content;
            setChanged();
            notifyObservers();
        }

        public Content getContent() {
            return content;
        }

    }

    public static void doObserverRequest(ObservableContent content) {
        SharedMethods.log("doCallbackRequest");

        long startTime = System.currentTimeMillis();
        SharedMethods.requestService.submit(() -> {
            content.setContent(SharedMethods.request());
        });

        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime: " + blockedTime + "ms");
    }

    public static void main(String[] args) {
        SharedMethods.log("main");
        HttpServer server = SharedMethods.server();
        ObservableContent content = new ObservableContent();
        content.addObserver(new ContentObserver(3));
        MultiObserverExample.doObserverRequest(content);
        MultiObserverExample.doObserverRequest(content);
        MultiObserverExample.doObserverRequest(content);

        long startTime = System.currentTimeMillis();
        lock.lock();
        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime for lock: " + blockedTime + "ms");

        SharedMethods.teardown(server);

        SharedMethods.log("finish");
    }

}
