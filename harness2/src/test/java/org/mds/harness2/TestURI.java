package org.mds.harness2;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Created by modongsong on 2014/10/24.
 */
public class TestURI {
    public static void main(String args[]) throws MalformedURLException {

        //invalid
        //System.out.println(new URL("127.0.0.1").getHost());
        System.out.println(new URL("http://http://127.0.0.1").getHost());
        System.out.println(new URL("http://test:9090").getHost());
        System.out.println(new URL("http://test").getHost());
        System.out.println(new URL("http:///test:9090").getHost());
        //invalid
        //System.out.println(new URL("/test").getHost());

        System.out.println(URI.create("127.0.0.1").getHost());
        System.out.println(URI.create("http://http://127.0.0.1").getHost());
        System.out.println(URI.create("http://test:9090").getHost());
        System.out.println(URI.create("http://test").getHost());
        System.out.println(URI.create("/test").getHost());
    }
}
