/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.siddhi.extension.execution.json;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.util.EventPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class IsExistsJSONFunctionTestCase {
    private static final Logger log = LogManager.getLogger(IsExistsJSONFunctionTestCase.class);
    private static final String JSON_INPUT = "{name:\"John\", age:25, citizen:false}";
    private AtomicInteger count = new AtomicInteger(0);

    @BeforeMethod
    public void init() {
        count.set(0);
    }

    @Test
    public void testIsExistsFromJSON() throws InterruptedException {
        log.info("IsExistsJSONFunctionTestCase - testIsExistsFromJSON");
        SiddhiManager siddhiManager = new SiddhiManager();
        String stream = "define stream InputStream(json string,path string);\n";
        String query = ("@info(name = 'query1')\n" +
                "from InputStream\n" +
                "select json:isExists(json, path) as value\n" +
                "insert into OutputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream + query);
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents,
                                Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    switch (count.get()) {
                        case 1:
                            AssertJUnit.assertEquals(true, event.getData(0));
                            break;
                        case 2:
                            AssertJUnit.assertEquals(true, event.getData(0));
                            break;
                        case 3:
                            AssertJUnit.assertEquals(true, event.getData(0));
                            break;
                        case 4:
                            AssertJUnit.assertEquals(false, event.getData(0));
                            break;
                    }
                }
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{JSON_INPUT, "$.age"});
        inputHandler.send(new Object[]{JSON_INPUT, "$.citizen"});
        inputHandler.send(new Object[]{JSON_INPUT, "$.name"});
        inputHandler.send(new Object[]{"{name:\"John\", age:25,", "$.age"});
        inputHandler.send(new Object[]{JSON_INPUT, "$.married"});
        Assert.assertEquals(count.get(), 4);
        siddhiAppRuntime.shutdown();
    }

}
