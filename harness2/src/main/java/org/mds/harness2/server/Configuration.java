package org.mds.harness2.server;

/**
 * @author Dongsong
 */
public class Configuration {
    int mode = 0;
    int port = 9090;
    int bossThreads = 1;
    int workers = 16;
    int acceptCount = 16;
    int handlerCount = 8;
    String responseFile = "response.m3u8";
}
