package org.mds.harness2;

import javolution.text.Cursor;
import javolution.text.Text;
import org.apache.http.client.utils.URIUtils;
import org.mds.harness2.tools.httpbench.PlayListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dongsong
 */
public class TestRegex {
    private final static Logger log = LoggerFactory.getLogger(TestRegex.class);
    private static final String PATTERN = "#EXT: *((Name *= *(?<name>\\S+)|Type *= *(?<type>\\S+)|SignalID *= *(?<signalId>\\S+)|URI *= *(?<uri>\\S+)|Duration *= *(?<duration>\\S+))( *, *| *$))+";
    private static final Pattern PATTERN1 = Pattern.compile("^#EXTINF: *(?<duration>\\S+) *,(?<title>.*)$");
    private static final String PATTERN_STRING = "^%s:((%s=(?<programId>\\d+)|%s=(?<bandwidth>\\d+)|%s=(?<codecs>\".*\")|%s=(?<resolution>\\w+)|%s=(?<audio>\".*\")|%s=(?<video>\".*\"))(,|$))+";


    @Test
    public void test() {
        Pattern pattern = Pattern.compile(String.format(PATTERN_STRING, "#extinf", "programId", "bandwidth", "codecs", "resolution", "audio", "video"));
        Matcher matcher = pattern.matcher("#extinf:programId=1,bandwidth=128,codecs=\"mp.dkd,jklli.kkd\",resolution=1024x1235,audio=\"fjdkl.fjdk,jkfdl\"");

        log.info(matcher.matches() + matcher.group("programId") + matcher.group("bandwidth") + matcher.group("codecs") + matcher.group("resolution"));
    }

    @Test
    public void test1() {
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher("#EXT: SignalID=9877");
        Assert.assertTrue(matcher.matches());
        Assert.assertTrue("9877".equals(matcher.group("signalId")));
        Matcher matcher1 = PATTERN1.matcher("#EXTINF: 6.000, Test File ");
        Assert.assertTrue(matcher1.matches());
        Assert.assertEquals("6.000", matcher1.group("duration"));
        Assert.assertEquals("Test File", matcher1.group("title").trim());
        Matcher matcher2 = PATTERN1.matcher("#EXTINF: 6.000,");
        Assert.assertTrue(matcher2.matches());
        Assert.assertEquals("6.000", matcher2.group("duration"));
        String title = matcher2.group("title").trim();
        Assert.assertEquals(title, "");
        log.info("dddd---------------------------------");
        Text text = new Text("#EXTINF: SignalID=9877,type=vod,Name=test");
        Text attributes = text.subtext(text.indexOf(':') + 1);
        log.info(text.toString() + attributes.toString());
        Map<String, String> attributeMap = new HashMap<>();
        Cursor cursor = new Cursor();
        for (CharSequence token; (token = cursor.nextToken(attributes, ',')) != null; ) {
            Text keyValue = (Text) token;
            int index = keyValue.indexOf('=');
            Text key = keyValue.subtext(0, index).trim();
            Text value = keyValue.subtext(index + 1).trim();
            attributeMap.put(key.toString(), value.toString());
        }


        log.info(attributeMap.get("SignalID") + "," + attributeMap.get("type"));
    }

    @Test
    public void test2() throws URISyntaxException {
        String url1 = "http://127.0.0.1/./a/../b";
        String url2 = "./127.0.0.1";
        URI uri1 = new URI(url1).normalize();
        URI uri2 = new URI(url2).normalize();

        String uri3 = PlayListUtils.normalize(url1);
        String uri4 = PlayListUtils.normalize(url2);

        Pattern pattern = Pattern.compile("((/|^))\\./");
        url1 = pattern.matcher(url1).replaceAll("$1");
        url2 = pattern.matcher(url2).replaceAll("$1");
        log.info("{}-----{}", url1, url2);


        log.info("---{}-{}", uri1, uri2);
        log.info("---{}-{}", uri3, uri4);
    }
}
