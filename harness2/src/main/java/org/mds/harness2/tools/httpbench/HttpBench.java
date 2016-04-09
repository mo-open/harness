package org.mds.harness2.tools.httpbench;

import com.ning.http.client.*;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import org.mds.harness.common2.runner.dsm.DsmRunner;
import org.mds.hprocessor.processor.*;
import org.mds.video.hls.model.M3uPlayList;
import org.mds.video.hls.model.tags.ExtInfTag;
import org.mds.video.hls.utils.PlayListUtils.PlayListReMapper;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.util.EntityUtils;
import org.mds.harness.common2.runner.RunnerHelper;
import org.mds.harness2.utils.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Dongsong
 */
public class HttpBench extends DsmRunner<Configuration> {
    protected final static Logger log = LoggerFactory.getLogger(HttpBench.class);

    //private static ExecutorService executorService=Executors.newFixedThreadPool(300);
    private static ExecutorService executorService = new ThreadPoolExecutor(300, 300,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(100000));
    private final static AtomicLong lastPeakTime = new AtomicLong();

    private void parseResponse(String content, final Configuration conf) {
        try {
            StringReader stringReader = new StringReader(content);
            if (conf.parse) {
                Map<Class, M3uPlayList.UriReMapper> uriReMappers = null;
                switch (conf.remapType) {
                    case 0:
                        break;
                    case 1:
                        uriReMappers = new HashMap<>();
                        uriReMappers.put(ExtInfTag.class,
                                new PlayListReMapper(ExtInfTag.class, "http://127.0.0.1/test1",
                                        "http://127.0.0.1/test2"));
                        break;
                }
                final M3uPlayList playList = M3uPlayList.forLines("test", stringReader, uriReMappers);
                executorService.execute(() -> {
                    try {
                        long duration = System.currentTimeMillis() - lastPeakTime.get();
                        if (duration > conf.commonDuration) {
                            Thread.sleep(conf.consumerTime);
                            if (duration > conf.peakDuration + conf.peakDuration) {
                                lastPeakTime.set(System.currentTimeMillis());
                            }
                        } else {
                            Thread.sleep(10);
                        }
                    } catch (Exception ex) {

                    }
                    playList.emit();
                });
            }
        } catch (Exception ex) {
            log.error("failed to pass ", ex);
        }
    }

    private void parseResponse(InputStream inputStream, final Configuration conf) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            if (conf.parse) {
                Map<Class, M3uPlayList.UriReMapper> uriReMappers = null;
                switch (conf.remapType) {
                    case 0:
                        break;
                    case 1:
                        uriReMappers = new HashMap<>();
                        uriReMappers.put(ExtInfTag.class,
                                new PlayListReMapper(ExtInfTag.class, "http://127.0.0.1/test1",
                                        "http://127.0.0.1/test2"));
                        break;
                }
                final M3uPlayList playList = M3uPlayList.forLines("test", reader, uriReMappers);

                executorService.execute(() -> {
                    try {
                        long duration = System.currentTimeMillis() - lastPeakTime.get();
                        if (duration > conf.commonDuration) {
                            Thread.sleep(conf.consumerTime);
                            if (duration > conf.peakDuration + conf.peakDuration) {
                                lastPeakTime.set(System.currentTimeMillis());
                            }
                        } else {
                            Thread.sleep(10);
                        }
                    } catch (Exception ex) {

                    }
                    playList.emit();
                });
            }
            reader.close();
        } catch (Exception ex) {
            log.error("failed to pass ", ex);
        }
    }

    private class EventValue {
        private int index;
        private String content;
        private InputStream inputStream;
        private M3uPlayList playList;

        public EventValue(int index) {
            this.index = index;
        }
    }

    public void runDisruptorPipeline1(final Configuration conf) {
        final AtomicLong finishedCounter = new AtomicLong();
        final DisruptorProcessor<EventValue> processor = DisruptorProcessor.<EventValue>newBuilder()
                .setBufferSize(conf.bufferSize)
                .addNext(conf.parseThreads, (EventValue value) -> {
                    try {
                        if (conf.parse) {
                            if (conf.returnStream) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(value.inputStream));
                                M3uPlayList playList = M3uPlayList.forLines("test", reader);
                                value.playList = playList;
                            } else {
                                StringReader stringReader = new StringReader(value.content);
                                M3uPlayList playList = M3uPlayList.forLines("test", stringReader);
                                value.playList = playList;
                            }
                        }
                    } catch (Exception ex) {
                        log.error("failed to pass ", ex);
                    }
                }).addNext(conf.remapThreads, object -> {
                    try {
                        if (conf.parse && conf.remapType == 1) {
                            PlayListUtils.remapResourceUris(object.playList,
                                    "http://127.0.0.1/test1", "http://127.0.0.1/test2");
                        }
                        finishedCounter.incrementAndGet();
                        object.content = null;
                        object.inputStream = null;
                    } catch (Exception ex) {
                        log.error("failed to remap ", ex);
                    }

                }).build();

        this.runSingle("Http Sync Perftest", conf, (configuration, index) -> {
                    try {
                        EventValue eventValue = new EventValue((int) index);
                        if (!conf.returnStream) {
                            eventValue.content = TestHelper.getUri(3000, new URI(conf.httpURL));
                        } else {
                            eventValue.inputStream = TestHelper.getStream(3000, new URI(conf.httpURL));
                        }
                        processor.submit(eventValue);
                    } catch (Exception e) {
                        log.error("Failed to request: " + conf.httpURL + ", " + e);
                    } finally {
                        // Release the connection.
                    }

                    return 1;
                }, finishedCounter
        );
    }

    public void runDisruptorPipeline2(final Configuration conf) {
        final AtomicLong finishedCounter = new AtomicLong();
        final DisruptorProcessor<EventValue> processor = DisruptorProcessor.<EventValue>newBuilder()
                .setBufferSize(conf.bufferSize)
                .addNext(conf.parseThreads, value -> {
                    try {
                        M3uPlayList playList;
                        if (conf.parse) {
                            if (conf.returnStream) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(value.inputStream));
                                playList = M3uPlayList.forLines("test", reader);
                            } else {
                                StringReader stringReader = new StringReader(value.content);
                                playList = M3uPlayList.forLines("test", stringReader);
                            }
                            if (conf.remapType == 1) {
                                PlayListUtils.remapResourceUris(playList,
                                        "http://127.0.0.1/test1", "http://127.0.0.1/test2");
                            }
                            finishedCounter.incrementAndGet();
                            value.content = null;
                            value.inputStream = null;
                        }
                    } catch (Exception ex) {
                        log.error("failed to pass ", ex);
                    }
                }).build();

        this.runSingle("Http Sync Perftest", conf, (configuration, index) -> {
            try {
                EventValue eventValue = new EventValue((int) index);
                if (!conf.returnStream) {
                    eventValue.content = TestHelper.getUri(3000, new URI(conf.httpURL));
                } else {
                    eventValue.inputStream = TestHelper.getStream(3000, new URI(conf.httpURL));
                }
                processor.submit(eventValue);
            } catch (Exception e) {
                log.error("Failed to request: " + conf.httpURL + ", " + e);
            } finally {
                // Release the connection.
            }

            return 1;
        }, finishedCounter);
    }

    public void runQueuePipeline1(final Configuration conf) {
        final AtomicLong finishedCounter = new AtomicLong();
        final BlockingQueueProcessor<EventValue> processor = BlockingQueueProcessor.<EventValue>newBuilder()
                .addNext(conf.parseThreads, value -> {

                    try {
                        if (conf.parse) {
                            if (conf.returnStream) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(value.inputStream));
                                M3uPlayList playList = M3uPlayList.forLines("test", reader);
                                value.playList = playList;
                            } else {
                                StringReader stringReader = new StringReader(value.content);
                                M3uPlayList playList = M3uPlayList.forLines("test", stringReader);
                                value.playList = playList;
                            }
                        }
                    } catch (Exception ex) {
                        log.error("failed to pass ", ex);
                    }
                }).addNext(conf.remapThreads, object -> {

                    try {
                        if (conf.parse && conf.remapType == 1) {
                            PlayListUtils.remapResourceUris(object.playList,
                                    "http://127.0.0.1/test1", "http://127.0.0.1/test2");
                        }
                        finishedCounter.incrementAndGet();
                        object.content = null;
                        object.inputStream = null;
                    } catch (Exception ex) {
                        log.error("failed to remap ", ex);
                    }
                }).build();

        this.runSingle("Http Sync Perftest", conf, (configuration, index) -> {
            try {
                EventValue eventValue = new EventValue((int) index);
                if (!conf.returnStream) {
                    eventValue.content = TestHelper.getUri(3000, new URI(conf.httpURL));
                } else {
                    eventValue.inputStream = TestHelper.getStream(3000, new URI(conf.httpURL));
                }
                processor.submit(eventValue);
            } catch (Exception e) {
                log.error("Failed to request: " + conf.httpURL + ", " + e);
            } finally {
                // Release the connection.
            }

            return 1;
        }, finishedCounter);
    }

    public void runQueuePipeline2(final Configuration conf) {
        final AtomicLong finishedCounter = new AtomicLong();
        final BlockingQueueProcessor<EventValue> processor =
                BlockingQueueProcessor.<EventValue>newBuilder()
                        .addNext(conf.parseThreads, value -> {
                            try {
                                M3uPlayList playList;
                                if (conf.parse) {
                                    if (conf.returnStream) {
                                        BufferedReader reader = new BufferedReader(new InputStreamReader(value.inputStream));
                                        playList = M3uPlayList.forLines("test", reader);
                                    } else {
                                        StringReader stringReader = new StringReader(value.content);
                                        playList = M3uPlayList.forLines("test", stringReader);
                                    }
                                    if (conf.remapType == 1) {
                                        PlayListUtils.remapResourceUris(playList,
                                                "http://127.0.0.1/test1", "http://127.0.0.1/test2");
                                    }
                                    finishedCounter.incrementAndGet();
                                }
                            } catch (Exception ex) {
                                log.error("failed to pass ", ex);
                            }
                        }).build();

        this.runSingle("Http Sync Perftest", conf, (configuration, index) -> {
            try {
                EventValue eventValue = new EventValue((int) index);
                if (!conf.returnStream) {
                    eventValue.content = TestHelper.getUri(3000, new URI(conf.httpURL));
                } else {
                    eventValue.inputStream = TestHelper.getStream(3000, new URI(conf.httpURL));
                }
                processor.submit(eventValue);
            } catch (Exception e) {
                log.error("Failed to request: " + conf.httpURL + ", " + e);
            } finally {
                // Release the connection.
            }

            return 1;
        }, finishedCounter);
    }

    public void runDisruptorPipeline3(final Configuration conf) {
        final AtomicLong finishedCounter = new AtomicLong();
        final DisruptorProcessor2<EventValue> processor = DisruptorProcessor2.<EventValue>newBuilder()
                .setBufferSize(conf.bufferSize)
                .addNext(conf.parseThreads, value -> {
                    try {
                        if (conf.parse) {
                            if (conf.returnStream) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(value.inputStream));
                                M3uPlayList playList = M3uPlayList.forLines("test", reader);
                                value.playList = playList;
                            } else {
                                StringReader stringReader = new StringReader(value.content);
                                M3uPlayList playList = M3uPlayList.forLines("test", stringReader);
                                value.playList = playList;
                            }
                        }
                    } catch (Exception ex) {
                        log.error("failed to pass ", ex);
                    }
                }).addNext(conf.remapThreads, object -> {
                    try {
                        if (conf.parse && conf.remapType == 1) {
                            PlayListUtils.remapResourceUris(object.playList,
                                    "http://127.0.0.1/test1", "http://127.0.0.1/test2");
                        }
                        finishedCounter.incrementAndGet();
                        object.content = null;
                        object.inputStream = null;
                    } catch (Exception ex) {
                        log.error("failed to remap ", ex);
                    }
                }).build();

        this.runSingle("Http Sync Perftest", conf, (configuration, index) -> {
            try {
                EventValue eventValue = new EventValue((int) index);
                if (!conf.returnStream) {
                    eventValue.content = TestHelper.getUri(3000, new URI(conf.httpURL));
                } else {
                    eventValue.inputStream = TestHelper.getStream(3000, new URI(conf.httpURL));
                }
                processor.submit(eventValue);
            } catch (Exception e) {
                log.error("Failed to request: " + conf.httpURL + ", " + e);
            } finally {
                // Release the connection.
            }

            return 1;
        }, finishedCounter);
    }

    public void runSync1(final Configuration conf) {
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoKeepAlive(true)
                .setSoReuseAddress(true)
                .setTcpNoDelay(true)
                .setSoTimeout(3000)
                .build();
        final ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setBufferSize(8 * 1024)
                .setFragmentSizeHint(8 * 1024)
                .build();
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(conf.asyncMaxTotal);
        cm.setDefaultMaxPerRoute(20);
        cm.setDefaultConnectionConfig(connectionConfig);
        final HttpHost routeHost = new HttpHost(conf.host, conf.port);
        cm.setSocketConfig(routeHost, socketConfig);

        cm.setMaxPerRoute(new HttpRoute(routeHost), conf.asyncMaxPerRoute);
        final AtomicInteger counter = new AtomicInteger();
        final CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();

        this.runSingle("Http Sync Perftest", conf, (configuration1, index1) -> {
            // Create a method instance.
            long startTime = System.currentTimeMillis();
            HttpGet method = new HttpGet(conf.httpURL);
            try {
                method.setHeader("Cookie", "zone=1;location=beijing");
                method.setHeader("Connection", "Keep-Alive");
                method.setHeader("X-Forwarded-For", "192.168.205.1");
                // Execute the method.
                CloseableHttpResponse response = httpclient.execute(method);

                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                    log.error("Failed request: " + statusLine.getReasonPhrase());
                }
                if (conf.parse)
                    parseResponse(EntityUtils.toString(response.getEntity()), conf);
                long spentTime = System.currentTimeMillis() - startTime;
                if (spentTime > conf.maxThreshold) {
                    log.warn("long response time:" + spentTime);
                }
                int count = counter.getAndIncrement();
                if (count % conf.statCount == 0) {
                    log.info("httpclient status:" + cm.getTotalStats());
                }
            } catch (Exception e) {
                log.error("Failed to request: " + conf.httpURL + ", " + e);
            } finally {
                // Release the connection.
                method.releaseConnection();
            }
            return 1;
        });

        try {
            httpclient.close();
        } catch (Exception ex) {

        }
    }

    public void runSync2(final Configuration conf) {
        System.setProperty("http.maxConnections", "" + conf.poolSize);
        this.runSingle("Http Sync Perftest", conf, (configuration1, index1) -> {
            try {
                long startTime = System.currentTimeMillis();
                if (!conf.returnStream) {
                    String content = TestHelper.getUri(conf.method, 3000,
                            new URI(conf.httpURL),
                            conf.disconnect,
                            conf.closeTimes,conf.parse);
                    if (conf.parse)
                        parseResponse(content, conf);
                } else {
                    parseResponse(TestHelper.getStream(3000, new URI(conf.httpURL)),
                            conf);
                }
                long spentTime = System.currentTimeMillis() - startTime;
                if (spentTime > conf.maxThreshold) {
                    log.warn("long response time:" + spentTime);
                }
            } catch (Exception e) {
                log.error("Failed to request: " + conf.httpURL + ", " + e);
            } finally {
                // Release the connection.
            }

            return 1;
        });
    }

    public void runVertx(final Configuration conf) {
        Vertx vertx = Vertx.vertx(new VertxOptions()
                .setEventLoopPoolSize(128)
                .setWorkerPoolSize(256)
        );
        HttpClient httpClient = vertx.createHttpClient(new HttpClientOptions()
                .setKeepAlive(true)
                .setMaxPoolSize(conf.poolSize)
                .setPipelining(conf.pipelining)
                .setReuseAddress(true)
                .setTcpNoDelay(conf.noDelay)
                .setKeepAlive(conf.keepalive)
        );
        final AtomicLong finishedCounter = new AtomicLong();
        this.runSingle("test vertx httpclient", conf, (configuration, index) -> {
            if (index % 100 == 0) {
                Thread.sleep(1);
            }
            for (int i = 0; i < conf.pipeCount; i++)
                httpClient.getAbs(conf.httpURL).handler(httpClientResponse -> {
                    finishedCounter.incrementAndGet();
                }).exceptionHandler(throwable -> {
                    log.info("Http Request failed: " + throwable);
                    finishedCounter.incrementAndGet();
                }).end();

            return conf.pipeCount;
        }, finishedCounter);
    }

    public void runAsync1(final Configuration conf) throws Exception {
        // Create I/O reactor configuration
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(Runtime.getRuntime().availableProcessors() + 1)
                .setConnectTimeout(3000)
                .setSoTimeout(3000)
                .setTcpNoDelay(true)
                .setSoKeepAlive(true)
                .setSoReuseAddress(true)
                .build();
        final ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setBufferSize(8 * 1024)
                .setFragmentSizeHint(8 * 1024)
                .build();
        // Create a custom I/O reactort
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connManager.setMaxTotal(conf.asyncMaxTotal);
        connManager.setDefaultMaxPerRoute(conf.asyncMaxPerRoute);
        connManager.setDefaultConnectionConfig(connectionConfig);
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost(conf.host, conf.port)), conf.asyncMaxPerRoute);

        final CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setConnectionManager(connManager)
                .build();
        httpclient.start();

        final AtomicLong finishedCounter = new AtomicLong();

        final FutureCallback<HttpResponse> futureCallback = new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                try {
                    parseResponse(EntityUtils.toString(result.getEntity()), conf);
                } catch (Exception ex) {

                }

                finishedCounter.incrementAndGet();
            }

            @Override
            public void failed(Exception ex) {
                log.info("Http Request failed: " + ex);
                finishedCounter.incrementAndGet();
            }

            @Override
            public void cancelled() {
                log.info("Http Request canceled: ");
                finishedCounter.incrementAndGet();
            }
        };

        this.runSingle("Http Sync Perftest", conf, (configuration1, index1) -> {
            // Create a method instance.
            long startTime = System.currentTimeMillis();
            HttpGet method = new HttpGet(conf.httpURL);
            try {
                method.setHeader("Cookie", "zone=1;location=beijing");
                method.setHeader("Connection", "Keep-Alive");
                method.setHeader("X-Forwarded-For", "192.168.205.1");
                HttpResponse result = httpclient.execute(method, futureCallback).get();
                long spentTime = System.currentTimeMillis() - startTime;
                if (spentTime > conf.maxThreshold) {
                    log.warn("long response time:" + spentTime);
                }

                try {
                    parseResponse(EntityUtils.toString(result.getEntity()), conf);
                } catch (Exception ex) {

                }
                // Read the response body.
                //byte[] responseBody = method.getResponseBody();
            } catch (Exception e) {
                log.error("Failed to request: " + conf.httpURL + ", " + e);
            } finally {
                // Release the connection.
                method.releaseConnection();
            }

            return 1;
        }, finishedCounter);

        httpclient.close();
    }

    private static AtomicInteger gettingCounter = new AtomicInteger();

    private class AsyncFutureCallback implements FutureCallback<HttpResponse> {
        HttpGet method;
        AtomicLong finishedCounter;
        long startTime;
        int maxThreshold;
        Configuration conf;

        public AsyncFutureCallback(HttpGet method,
                                   AtomicLong finishedCounter,
                                   Configuration conf) {
            this.method = method;
            this.finishedCounter = finishedCounter;
            this.conf = conf;
            startTime = System.currentTimeMillis();
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                parseResponse(EntityUtils.toString(result.getEntity()), this.conf);
            } catch (Exception ex) {

            }
            finishedCounter.incrementAndGet();
            long spentTime = System.currentTimeMillis() - startTime;
            if (spentTime > this.maxThreshold) {
                log.warn("long response time:" + spentTime);
            }
            this.method.releaseConnection();

            gettingCounter.decrementAndGet();
        }

        @Override
        public void failed(Exception ex) {
            log.info("Http Request failed: " + ex);
            finishedCounter.incrementAndGet();
            this.method.releaseConnection();
            gettingCounter.decrementAndGet();
        }

        @Override
        public void cancelled() {
            log.info("Http Request canceled: ");
            finishedCounter.incrementAndGet();
            this.method.releaseConnection();
            gettingCounter.decrementAndGet();
        }
    }

    public void runAsync2(final Configuration conf) throws Exception {
        // Create I/O reactor configuration
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(Runtime.getRuntime().availableProcessors() + 1)
                .setConnectTimeout(3000)
                .setSoTimeout(3000)
                .setSoKeepAlive(true)
                .setSoReuseAddress(true)
                .setTcpNoDelay(true)
                .build();
        final ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setBufferSize(8 * 1024)
                .setFragmentSizeHint(8 * 1024)
                .build();

        // Create a custom I/O reactort
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connManager.setMaxTotal(conf.asyncMaxTotal);
        connManager.setDefaultMaxPerRoute(conf.asyncMaxPerRoute);
        connManager.setDefaultConnectionConfig(connectionConfig);

        connManager.setMaxPerRoute(new HttpRoute(new HttpHost(conf.host, conf.port)), conf.asyncMaxPerRoute);

        final CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setConnectionManager(connManager)
                .build();
        httpclient.start();

        final AtomicLong finishedCounter = new AtomicLong();

        this.runSingle("Http Sync Perftest", conf, (configuration1, index1) -> {
            // Create a method instance.
            HttpGet method = new HttpGet(conf.httpURL);

            try {
                AsyncFutureCallback futureCallback = new AsyncFutureCallback(
                        method, finishedCounter, conf);
                method.setHeader("Cookie", "zone=1;location=beijing");
                method.setHeader("Connection", "Keep-Alive");
                method.setHeader("X-Forwarded-For", "192.168.205.1");
                httpclient.execute(method, futureCallback);
                gettingCounter.incrementAndGet();
                while (gettingCounter.get() > 3000) {
                    try {
                        Thread.sleep(1);
                    } catch (Exception ex) {
                    }
                }
                // Read the response body.
                //byte[] responseBody = method.getResponseBody();
            } catch (Exception e) {
                log.error("Failed to request: " + conf.httpURL + ", " + e);
                method.releaseConnection();
            } finally {
                // Release the connection.
            }

            return 1;
        }, finishedCounter);

        httpclient.close();
    }
}
