package org.mds.harness2.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.concurrent.Executors;

/**
 * @author Dongsong
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private AbstractHandler handler;

    public HttpServerInitializer(AbstractHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast("decoder", new HttpRequestDecoder());
        //pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        //pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        pipeline.addLast("deflate", new HttpContentCompressor(1));
        pipeline.addLast("handler", this.handler);
    }
}
