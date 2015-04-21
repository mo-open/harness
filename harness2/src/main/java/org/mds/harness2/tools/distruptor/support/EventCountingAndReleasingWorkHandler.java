/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mds.harness2.tools.distruptor.support;

import com.lmax.disruptor.EventReleaseAware;
import com.lmax.disruptor.EventReleaser;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.util.PaddedLong;

public final class EventCountingAndReleasingWorkHandler
        implements WorkHandler<ValueEvent>, EventReleaseAware {
    private final PaddedLong[] counters;
    private final int index;
    private EventReleaser eventReleaser;
    private long interval;

    public EventCountingAndReleasingWorkHandler(final PaddedLong[] counters, final int index) {
        this.counters = counters;
        this.index = index;
    }

    public EventCountingAndReleasingWorkHandler(final PaddedLong[] counters, final int index, final long interval) {
        this.counters = counters;
        this.index = index;
        this.interval = interval;
    }

    @Override
    public void onEvent(final ValueEvent event) throws Exception {
        eventReleaser.release();
        Thread.sleep(this.interval);
        counters[index].set(counters[index].get() + 1L);
    }

    @Override
    public void setEventReleaser(final EventReleaser eventReleaser) {
        this.eventReleaser = eventReleaser;
    }
}
