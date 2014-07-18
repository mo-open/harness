package org.mds.harness.tools.lang;

import javolution.text.Cursor;
import javolution.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.mds.harness.common.perf.PerfConfig;
import org.mds.harness.common.perf.PerfTester;
import org.mds.harness.common.runner.RunnerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import jregex.Matcher;
//import jregex.Pattern;

/**
 * @author Dongsong
 */
public class TestRegex {
    private final static Logger log = LoggerFactory.getLogger(TestRegex.class);
    protected static String EXTALLOWCACHE_TAG = "#EXT-X-ALLOW-CACHE";
    private final static Pattern tagPattern1 = Pattern.compile("^#EXT-X-ALLOW-CACHE: *(?<allowCache>YES|NO) *$");
    private final static Pattern tagPattern2 = Pattern.compile("^#EXT-X-ALLOW-CACHE:.*$");
    private final static Pattern pattern = Pattern.compile(".*<core:URI>(.*)</core:URI>.*");

//    private final static Pattern tagPattern1 = new Pattern("^#EXT-X-ALLOW-CACHE: *({allowCache}YES|NO) *$");
//    private final static Pattern tagPattern2 = new Pattern("^#EXT-X-ALLOW-CACHE:.*$");
//    private final static Pattern pattern = new Pattern(".*<core:URI>(.*)</core:URI>.*");

    private static final String PATTERN_STRING = "^#EXT:((Name=(?<name>.*)|Type=(?<type>.*)|SignalID=(?<signalId>.*)|URI=(?<uri>.*)|Duration=(?<duration>.*))(,|$))+";
    //    private static final String PATTERN_STRING = "^#EXT: *((Name=({name}.*)|Type=({type}.*)|SignalID *=({signalId}.*)|URI=({uri}.*)|Duration=({duration}.*))(,|$))+";
    private static final Pattern PATTERN2 = Pattern.compile("^#EXTINF: *(?<duration>\\S+) *,(?<title>.*)$");
    private final static Pattern PATTERN = Pattern.compile(PATTERN_STRING);

//    private static final Pattern PATTERN2 = new Pattern("^#EXTINF: *({duration}\\S+) *,({title}.*)$");
//    private final static Pattern PATTERN = new Pattern(PATTERN_STRING);

    private final static Random random = new Random();

    public void runSetNull(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test Regex (set null)", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                Matcher matcher = pattern.matcher("fdjl fdf<core:URI>(.*)</core:URI> fjdlll" + random.nextInt());
                matcher.matches();
                matcher.group(0);
                matcher = null;
                return 1;
            }
        });
        perfTester.run();
    }

    public void runUnsetNull(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test Regex (unset null)", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                pattern.matcher("fdjl fdf<core:URI>(.*)</core:URI> fjdlll" + random.nextInt()).matches();
                return 1;
            }
        });
        perfTester.run();
    }

    public void runReplaceAll(TestRegexConfiguration configuration) {
        final String url = "http://127.0.0.1:9090/linear/espn/test_";
        PerfTester perfTester = new PerfTester("Test Replace All", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                (url + index + ".m3u8").replaceAll("((/|^))\\./", "$1");
                return 1;
            }
        });
        perfTester.run();
    }

    public void runTextStartWith(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test Text startWith", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                new Text("#EXT-X-ALLOW-CACHE:YES").startsWith(EXTALLOWCACHE_TAG);
                new Text("#EXTM3U").startsWith(EXTALLOWCACHE_TAG);
                return 1;
            }
        });
        perfTester.run();
    }

    public void runStringStartWith(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test String startWith", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                "#EXT-X-ALLOW-CACHE:YES".startsWith(EXTALLOWCACHE_TAG);
                "#EXTM3U".startsWith(EXTALLOWCACHE_TAG);
                return 1;
            }
        });
        perfTester.run();
    }

    public void runMatchPattern(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test match Pattern", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                tagPattern2.matcher("#EXT-X-ALLOW-CACHE:YES").matches();
                tagPattern2.matcher("#EXTM3U").matches();
                return 1;
            }
        });
        perfTester.run();
    }

    public void runRegexParse1(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test regex parse1", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                try {
                    Matcher matcher = tagPattern1.matcher("#EXT-X-ALLOW-CACHE:YES");
                    matcher.matches();
                    matcher.group("allowCache");
                } catch (Exception ex) {
                    log.error("Failed to get allowCache on regexParse1");
                }
                return 1;
            }
        });
        perfTester.run();
    }

    public void runStringParse1(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test string parse1", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                StringUtils.substringAfter("#EXT-X-ALLOW-CACHE:YES", ":");
                return 1;
            }
        });
        perfTester.run();
    }

    public void runTextParse1(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test text parse 1", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                Text text = new Text("#EXT-X-ALLOW-CACHE:YES");
                text.subtext(text.indexOf(':') + 1);
                return 1;
            }
        });
        perfTester.run();
    }

    public void runRegexParse2(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test regex parse 2", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                try {
                    Matcher matcher = PATTERN.matcher("#EXT:SignalID=9877,type=vod");
                    matcher.matches();
                    matcher.group("signalId");
                    matcher.group("type");
                    matcher = null;
                } catch (Exception ex) {
                    log.error("Failed to get allowCache on regexParse2");
                }
                return 1;
            }
        });
        perfTester.run();
    }

    protected Map<String, String> getAttributeMap(String line) {
        //Map<String, String> attributeMap = new HashMap<>();
        String attributes = StringUtils.substringAfter(line, ":");
        if (StringUtils.contains(attributes, ",")) {
            String[] pairs = attributes.split(",");
            for (String pair : pairs) {
                String key = StringUtils.substringBefore(pair, "=");
                String value = StringUtils.substringAfter(pair, "=");
                //attributeMap.put(key, value);
            }
        } else {
            String key = StringUtils.substringBefore(attributes, "=");
            String value = StringUtils.substringAfter(attributes, "=");
            //attributeMap.put(key, value);
        }
        return null;
        //return attributeMap;
    }

    protected Map<String, String> getAttributeMap2(String line) {
        //Map<String, String> attributeMap = new HashMap<>();
        Text text = new Text(line);
        Text attributes = text.subtext(text.indexOf(':') + 1);
        Cursor cursor = new Cursor();
        for (CharSequence token; (token = cursor.nextToken(attributes, ',')) != null; ) {
            Text keyValue = (Text) token;
            int index = keyValue.indexOf('=');
            Text key = keyValue.subtext(0, index);
            Text value = keyValue.subtext(index + 1);
            key = null;
            value = null;
            //attributeMap.put(key.toString(), value.toString());
        }
        cursor = null;
        text = null;
        attributes = null;
        //return attributeMap;
        return null;
    }

    public void runStringParse2(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test String parse 2", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                getAttributeMap("#EXT:SignalID=9877,type=vod,Name=test");
                // Map<String, String> attributeMap = getAttributeMap("#EXT: SignalID=9877,type=vod,Name=test");
//                if (attributeMap.get("SignalID") == null) {
//                    log.error("Failed to get SignalID on stringParse2");
//                }
//
//                if (attributeMap.get("type") == null) {
//                    log.error("Failed to get type on stringParse2");
//                }
//                attributeMap=null;
                return 1;
            }
        });
        perfTester.run();
    }

    public void runTextParse2(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test Text parse 2", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
//                Map<String, String> attributeMap = getAttributeMap2("#EXT: SignalID=9877,type=vod,Name=test");
//
//                if (attributeMap.get("SignalID") == null) {
//                    log.error("Size:"+attributeMap.keySet().size());
//                    log.error("Failed to get SignalID on stringParse2");
//                }
//
//                if (attributeMap.get("type") == null) {
//                    log.error("Failed to get type on stringParse2");
//                }
//                attributeMap=null;
                getAttributeMap2("#EXT:SignalID=9877,type=vod,Name=test");
                return 1;
            }
        });
        perfTester.run();
    }

    public void runRegexParse3(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test regex parse 3", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                try {
                    Matcher matcher = PATTERN2.matcher("#EXTINF: 6.000,jfld jfdkl");
                    matcher.matches();
                    matcher.group("duration").trim();
                    matcher.group("title").trim();
                    matcher = null;
                } catch (Exception ex) {
                    log.error("Failed to get allowCache on regexParse3");
                }
                return 1;
            }
        });
        perfTester.run();
    }

    public void runStringParse3(TestRegexConfiguration configuration) {
        PerfTester perfTester = new PerfTester("Test String parse 3", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                String attributes = StringUtils.substringAfter("#EXTINF: 6.000,jfld jfdkl", ":");
                String[] values = attributes.split(",");
                values[0].trim();
                values[1].trim();
                return 1;
            }
        });
        perfTester.run();
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, TestRegex.class,
                TestRegexConfiguration.class, "testRegex.conf");
    }
}
