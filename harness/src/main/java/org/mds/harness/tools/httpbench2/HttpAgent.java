
package org.mds.harness.tools.httpbench2;


import java.net.URI;

public interface HttpAgent {

    void init() throws Exception;

    void shutdown() throws Exception;

    String getClientName();

    Stats get(URI target, int n, int c) throws Exception;

    Stats post(URI target, byte[] content, int n, int c) throws Exception;

}