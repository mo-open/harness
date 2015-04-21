package org.mds.harness2.tools.httpbench2;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Semaphore;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Request;

public class NingHttpClient implements HttpAgent {

    private AsyncHttpClient client;

    public NingHttpClient() {
        super();
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
        this.client.close();
    }

    Stats execute(final URI targetURI, final byte[] content, final int n, final int c) throws Exception {
        if (this.client != null) {
            this.client.close();
        }
        final AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
                .setAllowPoolingConnection(true)
                .setCompressionEnabled(false)
                .setMaximumConnectionsPerHost(c)
                .setMaximumConnectionsTotal(2000)
                .setRequestTimeoutInMs(15000)
                .build();
        this.client = new AsyncHttpClient(config);

        final Stats stats = new Stats(n, c);

        final Semaphore semaphore = new Semaphore(c);
        for (int i = 0; i < n; i++) {
            semaphore.acquire();
            Request request;
            if (content == null) {
                request = this.client.prepareGet(targetURI.toASCIIString())
                        .build();
            } else {
                request = this.client.preparePost(targetURI.toASCIIString())
                        .setBody(content)
                        .build();
            }
            try {
                this.client.executeRequest(request, new SimpleAsyncHandler(stats, semaphore));
            } catch (final IOException ex) {
                semaphore.release();
                stats.failure(0L);
            }
        }
        stats.waitFor();
        return stats;
    }

    @Override
    public Stats get(final URI target, final int n, final int c) throws Exception {
        return execute(target, null, n, c);
    }

    @Override
    public Stats post(final URI target, final byte[] content, final int n, final int c) throws Exception {
        return execute(target, content, n, c);
    }

    @Override
    public String getClientName() {
        return "Ning async HTTP client 1.8.8";
    }

    static class SimpleAsyncHandler implements AsyncHandler<Object> {

        private final Stats stats;
        private final Semaphore semaphore;

        private int status = 0;
        private long contentLen = 0;

        SimpleAsyncHandler(final Stats stats, final Semaphore semaphore) {
            super();
            this.stats = stats;
            this.semaphore = semaphore;
        }

        @Override
        public STATE onStatusReceived(final HttpResponseStatus responseStatus) throws Exception {
            this.status = responseStatus.getStatusCode();
            return STATE.CONTINUE;
        }

        @Override
        public STATE onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
            return STATE.CONTINUE;
        }

        @Override
        public STATE onBodyPartReceived(final HttpResponseBodyPart bodyPart) throws Exception {
            this.contentLen += bodyPart.getBodyPartBytes().length;
            return STATE.CONTINUE;
        }

        @Override
        public Object onCompleted() throws Exception {
            this.semaphore.release();
            if (this.status == 200) {
                this.stats.success(this.contentLen);
            } else {
                this.stats.failure(this.contentLen);
            }
            return STATE.CONTINUE;
        }

        @Override
        public void onThrowable(final Throwable t) {
            this.semaphore.release();
            this.stats.failure(this.contentLen);
        }

    }

    public static void main(final String[] args) throws Exception {
        final Config config = BenchRunner.parseConfig(args);
        BenchRunner.run(new NingHttpClient(), config);
    }

}