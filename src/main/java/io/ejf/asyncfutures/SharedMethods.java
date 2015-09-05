package io.ejf.asyncfutures;

import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.*;

import java.io.IOException;
import java.util.concurrent.*;

public class SharedMethods {
    public static ExecutorService serverService = Executors.newSingleThreadExecutor();
    public static ExecutorService requestService = Executors.newSingleThreadExecutor();

    public static HttpServer server() {
        log("server");
        HttpRequestHandler requestHandler = (request, response, context) -> {
            response.setEntity(new StringEntity("OK", "ASCII"));
            response.setStatusCode(HttpStatus.SC_OK);
        };
        HttpRequestHandler requestHandler1 = (request, response, context) -> {
            response.setEntity(new StringEntity("OK - 1", "ASCII"));
            response.setStatusCode(HttpStatus.SC_OK);
        };
        HttpRequestHandler requestHandler2 = (request, response, context) -> {
            response.setEntity(new StringEntity("OK - 2", "ASCII"));
            response.setStatusCode(HttpStatus.SC_OK);
        };
        HttpRequestHandler requestHandler3 = (request, response, context) -> {
            response.setEntity(new StringEntity("OK - 3", "ASCII"));
            response.setStatusCode(HttpStatus.SC_OK);
        };
        HttpProcessor httpProcessor=new ImmutableHttpProcessor(new ResponseDate(),new ResponseServer(),new ResponseContent(),new ResponseConnControl());

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(15000)
                .setTcpNoDelay(true)
                .build();
        final HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(8080)
                .setHttpProcessor(httpProcessor)
                .setSocketConfig(socketConfig)
                .registerHandler("*", requestHandler)
                .registerHandler("/1", requestHandler1)
                .registerHandler("/2", requestHandler2)
                .registerHandler("/3", requestHandler3)
                .create();

        serverService.submit(() -> {
            log("server start");
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
                server.shutdown(0L, TimeUnit.MILLISECONDS);
            }

            try {
                server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
            }

            log("server running");

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    log("shutting down server");
                    server.shutdown(0L, TimeUnit.MILLISECONDS);
                }
            });
        });

        return server;
    }

    public static Content request() {
        log("request");
        try {
            return Request.Get("http://127.0.0.1:8080")
                    .execute().returnContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Content request(String s) {
        log("request");
        try {
            return Request.Get("http://127.0.0.1:8080/" + s)
                    .execute().returnContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void log(String statement) {
        System.out.println(Thread.currentThread().getName() + ": " + statement);
    }

    public static void teardown(HttpServer server) {
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

        serverService.shutdownNow();
        requestService.shutdownNow();

        while (!serverService.isShutdown()
                || !requestService.isShutdown()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
