package es.omarall.redisxmqttbridge;

import es.omarall.redisxmqttbridge.model.BridgeDefinition;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Data
public class ApplicationProperties {
    private List<BridgeDefinition> bridgeDefinitions;
}
