package org.mds.harness.common2.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.io.Closeables;
import org.apache.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.testng.Assert.assertTrue;

/**
 * @author Randall.mo
 */
public class HttpAsyncSenderTest {
    private static final Logger log = LoggerFactory.getLogger(HttpAsyncSenderTest.class);
    WireMockServer wireMockServer;
    HttpAsyncSender sender;

    @BeforeClass
    public void setup() {
        //No-args constructor will start on port 8080, no HTTPS
        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();
        configureFor("localhost", 8089);
    }

    @AfterClass
    public void tearDown() throws IOException {
        wireMockServer.stop();
    }

    public void testSend(HttpAsyncSender sender) throws Exception {
        stubFor(post(urlEqualTo("/sendXml")).willReturn(aResponse().withStatus(200)));
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean isResponse = new AtomicBoolean(false);
        sender.send("http://localhost:8089/sendXml", "<sendXml/>", ContentTypeEnum.TEXT_XML, true, null);
        sender.send("http://localhost:8089/sendXml", "<sendXml/>", ContentTypeEnum.TEXT_XML, true, new HttpAsyncSender.SenderCallBack() {
            @Override
            public void completed(HttpResponse httpResponse) {
                isResponse.set(true);
                countDownLatch.countDown();
            }

            @Override
            public void failed(Exception e) {
                log.error("Test Failed", e);
            }

            @Override
            public void cancelled() {
            }
        });
        countDownLatch.await(3, TimeUnit.SECONDS);
        assertTrue(isResponse.get());

    }

    @Test
    public void testSendWithoutProcessor() throws Exception {
        HttpAsyncSender sender = new HttpAsyncSender();
        testSend(sender);
        Closeables.close(sender, true);
    }
}
