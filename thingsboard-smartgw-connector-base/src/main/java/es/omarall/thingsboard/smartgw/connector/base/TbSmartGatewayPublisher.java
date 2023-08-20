package es.omarall.thingsboard.smartgw.connector.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class TbSmartGatewayPublisher {
    private final TbSmartGatewayProperties tbSmartGatewayProperties;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public void sendTelemetry(String deviceName, List<TsValues> tsValues) {
        try {
            ObjectNode telemetry = objectMapper.createObjectNode();
            telemetry.set(deviceName, objectMapper.valueToTree(tsValues));
            publish(tbSmartGatewayProperties.getTelemetryKey(), objectMapper.writeValueAsString(telemetry));
        } catch (Exception e) {
            throw new RuntimeException("Cannot serialize Json node");
        }
    }

    public void sendAttributes(String deviceName, ObjectNode attributeValues) {
        try {
            ObjectNode attributes = objectMapper.createObjectNode();
            attributes.set(deviceName, objectMapper.valueToTree(attributeValues));
            publish(tbSmartGatewayProperties.getAttributesKey(), objectMapper.writeValueAsString(attributes));
        } catch (Exception e) {
            throw new RuntimeException("Cannot serialize Json node");
        }
    }

    private void publish(String streamKey, String payload) {
        log.debug("Publishing to topic: {} - payload: {}", streamKey, payload);
        String uniqueId = UUID.randomUUID().toString();
        RecordId recordId = redisTemplate.opsForStream().add(streamKey, Collections.singletonMap(uniqueId, payload));
        if (recordId != null) {
            log.debug("Message Succesfully Published. RecordId {}", recordId.getValue());
        } else {
            throw new IllegalStateException("RecordId should not be null. Null when xadd used in pipeline");
        }
    }

}
