package org.mds.harness.common.http;

import com.google.common.io.Closeables;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.mds.hprocessor.processor.Processor;
import org.mds.hprocessor.processor.ProcessorFactory;
import org.mds.hprocessor.processor.ProcessorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Randall.mo on 14-4-21.
 */
public class HttpAsyncSender implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(HttpAsyncSender.class);

    public static final int DEFAULT_TIMEOUT = 3000;

    private CloseableHttpAsyncClient httpclient;
    private int asyncMaxTotal = 2000;
    private int asyncMaxPerRoute = 10;
    private int timeout = 2000;
    private Processor<SendObject> processor;

    public static class SendObject {
        String url;
        String body;
        ContentTypeEnum contentType;
        boolean keepAlive;
        SenderCallBack callBack;
    }

    public HttpAsyncSender() {
        this(DEFAULT_TIMEOUT, null);
    }

    public HttpAsyncSender(ProcessorFactory<SendObject> processorFactory) {
        this(DEFAULT_TIMEOUT, processorFactory);
    }

    private HttpAsyncSender(int timeout, ProcessorFactory<SendObject> processorFactory) {
        this.timeout = timeout;
        if (processorFactory != null)
            this.processor = processorFactory.newProcessor(new ProcessorHandler<SendObject>() {
                @Override
                public void process(SendObject object) {
                    sendObject(object);
                }
            });
        this.startHttpClient();
    }

    private void checkStatus() {
        if (this.httpclient == null || !this.httpclient.isRunning()) {
            startHttpClient();
        }
    }

    private synchronized void startHttpClient() {
        if (this.httpclient != null && this.httpclient.isRunning()) return;
        // Create I/O reactor configuration
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                .setConnectTimeout(this.timeout)
                .setSoTimeout(this.timeout)
                .setSoKeepAlive(true)
                .setSoReuseAddress(true).build();
        ConnectingIOReactor ioReactor;
        try {
            ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        } catch (IOReactorException e) {
            log.error("Failed to create IOReactor");
            return;
        }

        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connManager.setMaxTotal(asyncMaxTotal);
        connManager.setDefaultMaxPerRoute(asyncMaxPerRoute);
        httpclient = HttpAsyncClients.custom()
                .setConnectionManager(connManager)
                .build();
        httpclient.start();
    }

    public void syncSend(String url, String body, ContentTypeEnum contentType, boolean keepAlive, SenderCallBack callBack) {
        this.syncSend0(url, body, contentType, keepAlive, callBack);
    }

    public void send(String url, String body, ContentTypeEnum contentType, boolean keepAlive, final SenderCallBack callBack) {
        if (this.processor == null) {
            this.send0(url, body, contentType, keepAlive, callBack);
        } else {
            SendObject sendObject = new SendObject();
            sendObject.url = url;
            sendObject.body = body;
            sendObject.contentType = contentType;
            sendObject.keepAlive = keepAlive;
            sendObject.callBack = callBack;
            this.processor.submit(sendObject);
        }
    }

    private void sendObject(SendObject sendObject) {
        send0(sendObject.url, sendObject.body, sendObject.contentType, sendObject.keepAlive, sendObject.callBack);
    }

    private void syncSend0(String url, String body, ContentTypeEnum contentType, boolean keepAlive, SenderCallBack callBack) {
        HttpPost httpPost = new HttpPost(url);
        try {
            this.checkStatus();
            HttpEntity httpEntity = new ByteArrayEntity(body.getBytes("UTF-8"));

            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, contentType.value());
            if (keepAlive)
                httpPost.setHeader("Connection", "Keep-Alive");
            httpPost.setEntity(httpEntity);
            HttpResponse result = httpclient.execute(httpPost, null).get();
            if (callBack != null) {
                callBack.completed(result);
            }
            httpPost.releaseConnection();
        } catch (Exception ex) {

            log.error("Failed to send content {} to '{}'", body, url, ex);
            try {
                httpPost.releaseConnection();
            } catch (Exception ex1) {

            }
            if (callBack != null) {
                callBack.failed(ex);
            }
        }
    }

    private void send0(String url, String body, ContentTypeEnum contentType, boolean keepAlive, final SenderCallBack callBack) {
        final HttpPost httpPost = new HttpPost(url);
        try {
            this.checkStatus();
            HttpEntity httpEntity = new ByteArrayEntity(body.getBytes("UTF-8"));
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, contentType.value());
            if (keepAlive)
                httpPost.setHeader("Connection", "Keep-Alive");
            httpPost.setEntity(httpEntity);
            httpclient.execute(httpPost, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse result) {
                    if (callBack != null)
                        callBack.completed(result);
                    httpPost.releaseConnection();
                }

                @Override
                public void failed(Exception ex) {
                    if (callBack != null)
                        callBack.failed(ex);
                    httpPost.releaseConnection();
                }

                @Override
                public void cancelled() {
                    if (callBack != null)
                        callBack.cancelled();
                    httpPost.releaseConnection();
                }
            });

        } catch (Exception ex) {
            log.error("Failed to send content {} to '{}'", body, url, ex);
        }
    }

    public interface SenderCallBack {

        public void completed(HttpResponse httpResponse);

        public void failed(Exception e);

        public void cancelled();
    }

    @Override
    public void close() throws IOException {
        try {
            Closeables.close(this.httpclient, false);
        } catch (IOException e) {
            log.error("Failed to close async http client");
        }
    }
}
