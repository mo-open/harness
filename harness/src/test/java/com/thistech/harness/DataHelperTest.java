package com.thistech.harness;

import org.mds.harness.utils.TestHelper;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class DataHelperTest {
    @Test
    public void testChannels() throws Exception {
        List<String> channels = Arrays.asList(new String[]{"A", "B", "C"});
        assertEquals(channels, TestHelper.channels(3));
    }
}
