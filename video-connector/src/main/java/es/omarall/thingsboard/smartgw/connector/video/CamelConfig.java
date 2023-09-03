package es.omarall.thingsboard.smartgw.connector.video;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {

    @Bean
    public RouteBuilder routeBuilder(@Value("${video-connector.channel-name}") String channelName) {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("video-io://{{video-connector.channel-name}}?captureAddress={{video-connector.capture-address}}&analysisQuality={{video-connector.analysis-quality}}")
                        .bean("detectionService", "detect")
                        .bean("thingsboardClient", "send")
                        .routeId("video-detection-" + channelName);
            }
        };
    }
}
