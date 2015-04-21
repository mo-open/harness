/*
 * Copyright 2012 LMAX Ltd.
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
package org.mds.harness2.tools.distruptor;

import org.mds.harness2.tools.distruptor.support.*;
import com.lmax.disruptor.WorkHandler;

class ValueAdditionWorkHandler implements WorkHandler<ValueEvent>
{
    private long total;

    @Override
    public void onEvent(ValueEvent event) throws Exception
    {
        long value = event.getValue();
        total += value;
        String a = "";
        for (int i = 0; i < 10; i++) {
            a = a + i;
        }
    }

    public long getTotal()
    {
        return total;
    }
}
