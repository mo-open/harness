package org.mds.harness2.server;

import com.lmax.disruptor.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Executors;

import static com.lmax.disruptor.RingBuffer.createMultiProducer;


/**
 * @author Dongsong
 */
@ChannelHandler.Sharable
public class HttpServerDisruptorHandler extends AbstractHandler {
    private static final int BUFFER_SIZE = 1024 * 64;
    private RingBuffer<HandlerEvent> ringBuffer;
    private DisruptorHandler[] handlers;
    private WorkerPool<HandlerEvent> workerPool;

    public HttpServerDisruptorHandler(int handlerCount, String responseFile) {
        super(responseFile);
        handlers = new DisruptorHandler[handlerCount];
        ringBuffer = createMultiProducer(EVENT_FACTORY, BUFFER_SIZE, new BlockingWaitStrategy());
        for (int i = 0; i < handlerCount; i++) {
            handlers[i] = new DisruptorHandler();
        }
        workerPool = new WorkerPool<HandlerEvent>(ringBuffer,
                ringBuffer.newBarrier(),
                new FatalExceptionHandler(),
                handlers);
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        workerPool.start(Executors.newFixedThreadPool(handlerCount));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object e)
            throws Exception {
        long sequence = ringBuffer.next();
        HandlerEvent event = ringBuffer.get(sequence);
        event.ctx = ctx;
        event.e = e;
        ringBuffer.publish(sequence);
    }

    private class DisruptorHandler implements WorkHandler<HandlerEvent> {

        @Override
        public void onEvent(HandlerEvent event) throws Exception {
            HttpServerDisruptorHandler.super.channelRead0(event.ctx, event.e);
        }
    }


    private static class HandlerEvent {
        ChannelHandlerContext ctx;
        Object e;
    }

    private static final EventFactory<HandlerEvent> EVENT_FACTORY = new EventFactory<HandlerEvent>() {
        public HandlerEvent newInstance() {
            return new HandlerEvent();
        }
    };
}
