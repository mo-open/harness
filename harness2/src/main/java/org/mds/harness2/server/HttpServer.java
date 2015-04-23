package org.mds.harness2.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.mds.harness.common2.config.ConfigurationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpServer {
    private final static Logger log = LoggerFactory.getLogger(HttpServer.class);
    private ScheduledExecutorService monitorExecutor = Executors.newSingleThreadScheduledExecutor();
    private Future monitorTask;
    private AbstractHandler handler;
    private Configuration configuration;

    private class Monitor implements Runnable {
        AbstractHandler handler;

        public Monitor(AbstractHandler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                log.info("Current accepted count: " + this.handler.getCurrentCount());
            } catch (Exception ex) {
                log.error("" + ex);
            }
        }
    }

    public HttpServer(Configuration configuration) {
        this.configuration = configuration;
    }

    public AbstractHandler createHanlder() {
        switch (configuration.mode) {
            case 0:
                this.handler = new HttpServerHandler(configuration.responseFile);
                break;
            case 1:
                this.handler = new HttpServerQueueHandler(configuration.handlerCount, configuration.responseFile);
                break;
            case 2:
                this.handler = new HttpServerDisruptorHandler(configuration.handlerCount, configuration.responseFile);
        }
        return this.handler;
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // Configure the server.
            ServerBootstrap bootstrap = new ServerBootstrap();
            this.handler = createHanlder();
            HttpServerInitializer initializer = new HttpServerInitializer(this.handler);
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(initializer)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);

            this.monitorTask = this.monitorExecutor.scheduleAtFixedRate(
                    new Monitor(this.handler), 1, 1, TimeUnit.SECONDS);
            Channel ch = bootstrap.bind(configuration.port).sync().channel();
            ch.closeFuture().sync();
        } catch (Exception ex) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        Properties properties = ConfigurationHelper.parseInputArgs(args);
        Configuration configuration = (Configuration) ConfigurationHelper.loadYAMLConfiguration("http-server.yml", properties, Configuration.class);

        log.info("server start with port {}", configuration.port);
        new HttpServer(configuration).run();
    }
}