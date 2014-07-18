package org.mds.harness.server;

import com.google.common.io.Closeables;
import com.google.common.io.LineReader;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.*;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

import io.netty.util.CharsetUtil;
import org.apache.http.impl.nio.reactor.ExceptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpHeaders.getHost;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * @author Dongsong
 */
public class AbstractHandler extends SimpleChannelInboundHandler<Object> {
    private final static Logger log = LoggerFactory.getLogger(AbstractHandler.class);
    private AtomicInteger count = new AtomicInteger(0);
    private String responseFile;
    private StringBuilder responseBuffer = new StringBuilder();

    protected AbstractHandler(String responseFile) {
        this.responseFile = responseFile;
        this.genPlayList();
    }

    protected void increment() {
        int currentCount = count.incrementAndGet();
    }

    protected void decrement() {
        int currentCount = count.decrementAndGet();
    }

    public int getCurrentCount() {
        return this.count.get();
    }

    protected static void sendError(ChannelHandlerContext ctx,
                                    HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(
                "Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());
        response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);

        // Close the connection as soon as the error message is sent.
        ctx.write(response);
    }


    private void sendPrepare(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, OK, Unpooled.copiedBuffer(
                this.responseBuffer.toString(), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        ctx.write(response);
    }

    private void genPlayList() {
        InputStream inputStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(this.responseFile);
        if (inputStream == null) {
            log.error("Can not file " + this.responseFile);
            return ;
        }
        Reader reader = new BufferedReader(new InputStreamReader(inputStream));

        LineReader lineReader = new LineReader(reader);
        try {
            String line = null;
            while (true) {
                line = lineReader.readLine();
                if (line == null) {
                    break;
                }
                if ("".equals(line.trim())) continue;
                this.responseBuffer.append(line).append("\r\n");
            }
        } catch (Exception ex) {

        } finally {
            try {
                Closeables.close(reader, true);
            } catch (Exception ex) {

            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object e) throws Exception {
        if (e instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) e;
            if (request.getMethod() != HttpMethod.GET) {
                sendError(ctx, BAD_REQUEST);
                return;
            }
        }
        if (e instanceof LastHttpContent) {
            sendPrepare(ctx);
            increment();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }
}
