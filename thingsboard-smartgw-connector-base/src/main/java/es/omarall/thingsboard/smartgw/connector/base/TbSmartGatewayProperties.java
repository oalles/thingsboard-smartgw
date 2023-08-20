package es.omarall.thingsboard.smartgw.connector.base;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Define
 */
@ConfigurationProperties(prefix = "tb-smart-gateway")
@Data
public class TbSmartGatewayProperties {
    private String telemetryKey = "telemetry";
    private String attributesKey = "attributes";
    private String attributeUpdatesKey = "attributes.updates";
}
