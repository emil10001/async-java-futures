package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ObserverExample {
    private static ReentrantLock lock = new ReentrantLock();

    private static class ContentObserver implements Observer {
        @Override
        public void update(Observable o, Object arg) {
            if (!(o instanceof ObservableContent))
                return;

            SharedMethods.log("update result" + ((ObservableContent)o).getContent().asString());
            lock.unlock();
        }

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
        SharedMethods.log("doObserverRequest");

        long startTime = System.currentTimeMillis();
        SharedMethods.requestService.submit(() -> {
            try {
                content.setContent(SharedMethods.request());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime: " + blockedTime + "ms");
    }

    public static void main(String[] args) {
        SharedMethods.log("main");
        HttpServer server = SharedMethods.server();
        ObservableContent content = new ObservableContent();
        content.addObserver(new ContentObserver());
        ObserverExample.doObserverRequest(content);

        long startTime = System.currentTimeMillis();
        lock.lock();
        long blockedTime = (System.currentTimeMillis() - startTime);
        SharedMethods.log("blockedTime for lock: " + blockedTime + "ms");

        try {
            Thread.sleep(1000);
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
