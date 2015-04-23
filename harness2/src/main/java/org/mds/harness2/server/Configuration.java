package org.mds.harness2.server;

/**
 * @author Dongsong
 */
public class Configuration {
    public int mode = 0;
    public int port = 9090;
    public int bossThreads = 1;
    public int workers = 16;
    public int acceptCount = 16;
    public int handlerCount = 8;
    public String responseFile = "response.m3u8";
}
