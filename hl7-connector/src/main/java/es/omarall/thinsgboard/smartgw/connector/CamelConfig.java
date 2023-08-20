package es.omarall.thinsgboard.smartgw.connector;

import ca.uhn.hl7v2.model.v23.message.ACK;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hl7.HL7Constants;
import org.apache.camel.component.hl7.HL7MLLPNettyDecoderFactory;
import org.apache.camel.component.hl7.HL7MLLPNettyEncoderFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {

    @Bean
    public RouteBuilder routeBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                // Route to receive HL7 messages from the HL7 TCP port
                from("netty:tcp://{{hl7-server.hostname}}:{{hl7-server.port}}?sync=true&decoders=#hl7decoder&encoders=#hl7encoder")
                        .unmarshal().hl7(true)
                        .log(LoggingLevel.DEBUG, "\n\nHeaders: ${headers}\nHL7 Message: ${body}")
                        .choice()
                        .when(header(HL7Constants.HL7_TRIGGER_EVENT).isEqualTo(Constants.RO1_TRIGGER_EVENT))
                        .bean("tbOruR01Processor", "process")
                        .otherwise()
                        .process(exchange -> exchange.getIn().setBody(new ACK())) // TODO: Default Message processor
                        .end()
                        .marshal().hl7()
                        .log(LoggingLevel.DEBUG, "HL7 Response: ${body}")
                        .routeId("hl7-inbound-route");
            }
        };
    }

    @ConditionalOnProperty(name = "file-producer.enabled", havingValue = "true")
    @Bean
    public RouteBuilder fileInputRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {

                // Utility Route to send HL7 messages to HL7 server
                from("file:{{file-producer.input-dir}}?delete=true&timeUnit=SECONDS&delay={{file-producer.delay-in-seconds}}&noop=true")
                        .convertBodyTo(String.class)
                        .to("netty:tcp://{{hl7-server.hostname}}:{{hl7-server.port}}?sync=true&decoders=#hl7decoder&encoders=#hl7encoder")
                        .routeId("hl7-input-folder");
            }
        };
    }

    @Bean
    public HL7MLLPNettyDecoderFactory hl7decoder() {
        HL7MLLPNettyDecoderFactory decoderFactory = new HL7MLLPNettyDecoderFactory();
        decoderFactory.setConvertLFtoCR(true);
        return decoderFactory;
    }

    @Bean
    public HL7MLLPNettyEncoderFactory hl7encoder() {
        HL7MLLPNettyEncoderFactory encoderFactory = new HL7MLLPNettyEncoderFactory();
        encoderFactory.setConvertLFtoCR(true);
        return encoderFactory;
    }
}
