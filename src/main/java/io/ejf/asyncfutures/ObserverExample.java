package io.ejf.asyncfutures;

import org.apache.http.client.fluent.Content;
import org.apache.http.impl.bootstrap.HttpServer;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantLock;

public class ObserverExample {
    protected static ReentrantLock lock = new ReentrantLock();
    static final SharedMethods.Log log_ = new SharedMethods.Log(ObserverExample.class);

    protected static class ContentObserver implements Observer {

        @Override
        public void update(Observable o, Object arg) {
            if (!(o instanceof ObservableContent))
                return;

            log_.log("update result" + ((ObservableContent) o).getContent().asString());
            lock.unlock();
        }
    }

    protected static class ObservableContent extends Observable {
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
        log_.log("doCallbackRequest");

        long startTime = System.currentTimeMillis();
        SharedMethods.requestService.submit(() -> {
            content.setContent(SharedMethods.request());
        });

        long blockedTime = (System.currentTimeMillis() - startTime);
        log_.log("blockedTime: " + blockedTime + "ms");
    }

    public static void main(String[] args) {
        log_.log("main");
        HttpServer server = SharedMethods.server();
        ObservableContent content = new ObservableContent();
        content.addObserver(new ContentObserver());
        ObserverExample.doObserverRequest(content);

        long startTime = System.currentTimeMillis();
        lock.lock();
        long blockedTime = (System.currentTimeMillis() - startTime);
        log_.log("blockedTime for lock: " + blockedTime + "ms");

        SharedMethods.teardown(server);

        log_.log("finish");
    }

}
