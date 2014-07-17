package org.mds.harness.common.http.request;

import io.netty.handler.codec.http.HttpRequest;

/**
 * Created by Randall.mo on 14-6-19.
 */
public interface RequestBuilder {
    HttpRequest build();
}
