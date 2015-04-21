package org.mds.harness2.parallel;

import org.testng.annotations.Test;

/**
 * Created by modongsong on 14-6-12.
 */
public class MetricsHolderTest {

    @Test
    public void testInstance(){
        Metrics.initialize("name");
        Metrics.isEnabled();
    }
}
