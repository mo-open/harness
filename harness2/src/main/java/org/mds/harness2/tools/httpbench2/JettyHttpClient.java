package org.mds.harness2.tools.httpbench2;

import java.net.URI;
import java.util.concurrent.Semaphore;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.server.Server;

public class JettyHttpClient implements HttpAgent {

    private final HttpClient client;

    public JettyHttpClient() {
        super();
        this.client = new HttpClient();
        this.client.setRequestBufferSize(8 * 1024);
        this.client.setResponseBufferSize(8 * 1024);
        this.client.setConnectTimeout(3000);
        this.client.setTCPNoDelay(false);
        this.client.setFollowRedirects(false);
    }

    @Override
    public void init() throws Exception {
        this.client.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.client.stop();
    }

    Stats execute(final URI targetURI, final byte[] content, final int n, final int c) throws Exception {
        this.client.setMaxConnectionsPerDestination(c);
        final Stats stats = new Stats(n, c);
        final Semaphore semaphore = new Semaphore(c);
        for (int i = 0; i < n; i++) {
            semaphore.acquire();
            try {
                if (content != null) {
                    this.client.POST(targetURI).content(new BytesContentProvider(content), "text/plain").send();
                } else {
                    this.client.GET(targetURI);
                }
                stats.success(content.length);
            } catch (final Exception ex) {
                semaphore.release();
                stats.failure(0L);
            } finally {
                semaphore.release();
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
        return "Jetty " + Server.getVersion();
    }


    public static void main(final String[] args) throws Exception {
        final Config config = BenchRunner.parseConfig(args);
        BenchRunner.run(new JettyHttpClient(), config);
    }

}