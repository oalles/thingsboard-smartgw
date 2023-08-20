# Thingsboard SmartGateway Connector Base

This library provides the fundamental implementation for publishing Thingsboard data (telemetry and attribute updates)
to Redis Streams,
which aligns with the Thingsboard Gateway Redis API discussed earlier.

Additionally, it offers essential generic interfaces that define the contract for extracting critical device
information, such as device names and types, and payloads for telemetry and client attribute updates.

### TbSmartGatewayPublisher

The `TbSmartGatewayPublisher` class is responsible for publishing data to Redis Streams as per the Thingsboard Gateway
Redis API.
Every connector implementation should use this class to publish data to Redis Streams.

```java

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
```

### Extractor Interfaces

```java

public interface DeviceNameExtractor<T> {
    String extractDeviceName(T t);
}

public interface DeviceTypeExtractor<T> {
    String extractDeviceType(T t);
}

public interface TelemetryValuesExtractor<T> {
    List<TsValues> extractTelemetryValues(T t);
}

public interface AttributesExtractor<T> {
    ObjectNode extractAttributeValues(T t);
}
```




