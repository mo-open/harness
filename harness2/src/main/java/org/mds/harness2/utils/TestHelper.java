package org.mds.harness2.utils;

import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class TestHelper {
    private final static Logger log = LoggerFactory.getLogger(TestHelper.class);

    public static List<String> channels(int count) {
        int from = (int) 'A';
        List<String> channels = new ArrayList<String>();

        for (int i = 0; i < count; i++) {
            channels.add(String.valueOf((char) (from + i)));
        }
        return channels;
    }

    public static MemcachedClient connectMc(String addresses) {
        String[] addressArray = addresses.split(";");
        List<InetSocketAddress> mcAddressList = new ArrayList<>();
        for (String address : addressArray) {
            String[] hostNamePort = address.split(":");
            String hostName = hostNamePort[0];
            int mcPort = 11211;
            if (hostNamePort.length > 1) {
                try {
                    mcPort = Integer.parseInt(hostNamePort[1]);
                } catch (Exception ex) {
                }
            }
            mcAddressList.add(new InetSocketAddress(hostName, mcPort));
        }
        try {
            return new MemcachedClient(mcAddressList);
        } catch (Exception ex) {
            log.error("Failed to connect memcached,address list: " + addresses, ex);
        }
        return null;
    }

    public static String genClientIp(String prefix, int index) {
        int ip_0 = index / 255;
        int ip_1 = index % 255;

        return prefix + "." + ip_0 + "." + ip_1;
    }

    public static InputStream getStream(int timeout, URI uri) throws MalformedURLException {
        URL url = new URL(uri.toString());
        HttpURLConnection conn = null;
        BufferedReader in = null;
        try {
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestProperty("Connection", "Keep-Alive");
            return conn.getInputStream();
        } catch (IOException e) {
            log.error("Failed to get uri: " + uri + ", due to: " + e.getMessage(), e);
            if (conn != null) {
                conn.disconnect();
            }
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static String getUri(int timeout, URI uri) throws MalformedURLException {
        URL url = new URL(uri.toString());
        HttpURLConnection conn = null;
        BufferedReader in = null;
        try {
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestProperty("Connection", "Keep-Alive");
            InputStream inputStream = conn.getInputStream();
            StringBuffer content = new StringBuffer();
            if (conn.getResponseCode() == 200) {
                in = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine).append("\r\n");
                }
            }
            return content.toString();
        } catch (IOException e) {
            log.error("Failed to get uri: " + uri + ", due to: " + e.getMessage(), e);
            if (conn != null) {
                conn.disconnect();
            }
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static String postUri(int timeout, String uri, String content) throws MalformedURLException {
        URL url = new URL(uri.toString());
        HttpURLConnection conn = null;
        BufferedReader in = null;
        byte[] buf = content.getBytes();
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Length", String.valueOf(buf.length));
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.connect();

            OutputStream out = conn.getOutputStream();
            out.write(buf);
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            log.error("Failed to get uri: " + uri + ", due to: " + e.getMessage(), e);
            if (conn != null) {
                conn.disconnect();
            }
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (conn != null) {
                try {
                    // conn.disconnect();
                } catch (Exception e) {

                }
            }
        }
    }
}
