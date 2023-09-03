/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.omarall.camel.components.videoio;

import lombok.Getter;
import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.DefaultEndpoint;

@Getter
@UriEndpoint(firstVersion = "2.17.0", scheme = "video-io", title = "Video-IO",
        syntax = "nats:topic", category = {Category.IOT, Category.FILE},
        headersClass = VideoIOConstants.class)
public class VideoIOEndpoint extends DefaultEndpoint {
    @UriParam
    private final VideoIOConfiguration configuration;

    public VideoIOEndpoint(String uri, VideoIOComponent component, VideoIOConfiguration config) {
        super(uri, component);
        this.configuration = config;
    }

    @Override
    public Producer createProducer() {
        throw new UnsupportedOperationException("Producer not supported");
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        VideoIOConsumer consumer = new VideoIOConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

}