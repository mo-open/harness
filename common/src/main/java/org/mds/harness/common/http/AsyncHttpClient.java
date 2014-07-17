package org.mds.harness.common.http;

import org.mds.harness.common.http.request.RequestConfig;
import org.mds.hprocessor.processor.DisruptorProcessor;

/**
 * Created by Randall.mo on 14-6-19.
 */
public class AsyncHttpClient {
    private HttpClientConfig clientConfig;
    private RequestConfig defaultRequestConfig = null;
    private DisruptorProcessor requestProcessor;

    public AsyncHttpClient(HttpClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public AsyncHttpClient(HttpClientConfig clientConfig, RequestConfig defaultRequestConfig) {
        this.clientConfig = clientConfig;
        this.defaultRequestConfig = defaultRequestConfig;
    }

    public AsyncHttpClient setDefaultRequestConfig(RequestConfig defaultRequestConfig) {
        this.defaultRequestConfig = defaultRequestConfig;
        return this;
    }


}
