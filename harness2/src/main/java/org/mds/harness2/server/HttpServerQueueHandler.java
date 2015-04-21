package org.mds.harness2.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Dongsong
 */
@ChannelHandler.Sharable
public class HttpServerQueueHandler extends AbstractHandler {
    private ExecutorService executorService;

    public HttpServerQueueHandler(int handlers, String responseFile) {
        super(responseFile);
        this.executorService = Executors.newFixedThreadPool(handlers);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object e) throws Exception {
        this.executorService.submit(new HandlerWorker(ctx, e));
    }


    private class HandlerWorker implements Runnable {
        ChannelHandlerContext ctx;
        Object e;

        public HandlerWorker(ChannelHandlerContext ctx, Object e) {
            this.ctx = ctx;
            this.e = e;
        }

        @Override
        public void run() {
            try {
                HttpServerQueueHandler.super.channelRead0(ctx, e);
            } catch (Exception ex) {

            }
        }
    }
}
