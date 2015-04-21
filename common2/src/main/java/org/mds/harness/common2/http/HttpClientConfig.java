package org.mds.harness.common2.http;

/**
 * Created by Randall.mo on 14-6-19.
 */
public class HttpClientConfig {
    private int connections = 1;
    private String host;
    private int port;
    private boolean ssl;
    private int maxChunkSize;
    private int maxInitialLineLength;
    private boolean compress;

    public int getConnections() {
        return connections;
    }

    public HttpClientConfig setConnections(int connections) {
        this.connections = connections;
        return this;
    }

    public String getHost() {
        return host;
    }

    public HttpClientConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public HttpClientConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isSsl() {
        return ssl;
    }

    public HttpClientConfig setSsl(boolean ssl) {
        this.ssl = ssl;
        return this;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public HttpClientConfig setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        return this;
    }

    public int getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    public HttpClientConfig setMaxInitialLineLength(int maxInitialLineLength) {
        this.maxInitialLineLength = maxInitialLineLength;
        return this;
    }

    public boolean isCompress() {
        return compress;
    }

    public HttpClientConfig setCompress(boolean compress) {
        this.compress = compress;
        return this;
    }
}
