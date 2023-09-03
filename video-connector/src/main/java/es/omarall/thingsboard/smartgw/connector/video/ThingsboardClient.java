package es.omarall.thingsboard.smartgw.connector.video;

import ai.djl.modality.cv.output.DetectedObjects;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.omarall.camel.components.videoio.VideoIOConstants;
import es.omarall.thingsboard.smartgw.connector.base.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThingsboardClient implements DeviceNameExtractor<Map<String, Object>>,
        TelemetryValuesExtractor<DetectedObjects>, AttributesExtractor<Map<String, Object>> {

    private final TbSmartGatewayPublisher tbSmartGatewayPublisher;
    private final ObjectMapper objectMapper;

    public void send(@Body DetectedObjects detections, @Headers Map<String, Object> headers) {
        // 1. Extract Device Name from headers
        String deviceName = this.extractDeviceName(headers);

        // 2. Build Attributes from headers
        ObjectNode attributes = this.extractAttributeValues(headers);

        // 3. Build Telemetry from detections
        List<TsValues> telemetryValues = this.extractTelemetryValues(detections);

        // Send to TB via Redis Gw Api
        tbSmartGatewayPublisher.sendTelemetry(deviceName, telemetryValues);
        tbSmartGatewayPublisher.sendAttributes(deviceName, attributes);
    }

    @Override
    public String extractDeviceName(Map<String, Object> headers) {
        return (String) headers.get(VideoIOConstants.VIDEO_IO_CHANNEL_NAME);
    }

    @Override
    public List<TsValues> extractTelemetryValues(DetectedObjects detections) {

        final long detectionTimeInMillis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        if (!detections.items().isEmpty()) {
            try {
                JsonNode jsonNode = objectMapper.readTree(detections.toJson());

                final TelemetryPayload telemetryPayload = TelemetryPayload.builder()
                        .detections(jsonNode)
                        .totalCount(detections.getNumberOfObjects())
                        .carCount(0)
                        .personCount(0)
                        .build();

                // Perform calculations based on class
                detections.items().stream().filter(i -> i.getClassName().equals("person") || i.getClassName().equals("car"))
                        .forEach(item -> {
                            if (item.getClassName().equals("person")) {
                                telemetryPayload.setPersonCount(telemetryPayload.getPersonCount() + 1);
                            } else if (item.getClassName().equals("car")) {
                                telemetryPayload.setCarCount(telemetryPayload.getCarCount() + 1);
                            }
                        });

                ObjectNode values = objectMapper.valueToTree(telemetryPayload);
                return Collections.singletonList(TsValues.builder().ts(detectionTimeInMillis).values(values).build());
            } catch (Exception e) {
                log.error("Error parsing detections: {}", detections, e);
            }
        }
        return List.of();
    }

    @Override
    public ObjectNode extractAttributeValues(Map<String, Object> headers) {
        ObjectNode on = objectMapper.createObjectNode();
        on.put("captureAddress", (String) headers.get(VideoIOConstants.VIDEO_IO_CHANNEL_CAPTURE_ADDRESS));
        return on;
    }

    @Data
    @Builder
    public static class TelemetryPayload {
        private JsonNode detections; // as they come
        private long totalCount;
        private long carCount;
        private long personCount;
    }
}
