package org.mds.harness2.server;

import io.netty.channel.ChannelHandler;

@ChannelHandler.Sharable
public class HttpServerHandler extends AbstractHandler {
    protected HttpServerHandler(String responseFile) {
        super(responseFile);
    }
}