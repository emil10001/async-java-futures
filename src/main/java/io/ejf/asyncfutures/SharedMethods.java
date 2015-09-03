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
                e.printStackTrace();
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

    public static Content request() throws IOException {
        log("request");
        return Request.Get("http://127.0.0.1:8080")
                .execute().returnContent();
    }

    public static void log(String statement) {
        System.out.println(Thread.currentThread().getName() + ": " + statement);
    }

}
