package org.mds.harness.server;

import io.netty.channel.ChannelHandler;

@ChannelHandler.Sharable
public class HttpServerHandler extends AbstractHandler {
    protected HttpServerHandler(String responseFile) {
        super(responseFile);
    }
}