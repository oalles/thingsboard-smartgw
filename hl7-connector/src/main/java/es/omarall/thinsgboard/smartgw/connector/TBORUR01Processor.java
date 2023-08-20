package es.omarall.thinsgboard.smartgw.connector;

import ca.uhn.hl7v2.model.v23.message.ACK;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.omarall.thingsboard.smartgw.connector.base.*;
import es.omarall.thinsgboard.smartgw.connector.hl7.ORUR01Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.apache.camel.component.hl7.HL7Constants;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service("tbOruR01Processor")
@RequiredArgsConstructor
@Slf4j
public class TBORUR01Processor implements DeviceNameExtractor<Map<String, Object>>,
        AttributesExtractor<Map<String, Object>>,
        TelemetryValuesExtractor<ORU_R01> {

    private final TbSmartGatewayPublisher tbSmartGatewayPublisher;
    private final ObjectMapper objectMapper;
    private final ORUR01Utils oruR01Utils;

    public ACK process(@Body ORU_R01 oruMessage, @Headers Map<String, Object> headers) {

        // Optionally: Send to HMS (Hospital Management System) here or in a separate route or processor
        // ...

        // And send to TB

        // 1. Extract Device Name
        String deviceName = this.extractDeviceName(headers);

        // 2. Extract attributes
        ObjectNode attributes = this.extractAttributeValues(headers);

        // 3. Build Telemetry
        List<TsValues> telemetryValues = this.extractTelemetryValues(oruMessage);

        // Send to TB via Redis Gw Api
        tbSmartGatewayPublisher.sendTelemetry(deviceName, telemetryValues);
        tbSmartGatewayPublisher.sendAttributes(deviceName, attributes);

        return this.buildAckFromHeaders(headers);
    }

    @Override
    public String extractDeviceName(Map<String, Object> headers) {
        String sendingApp = (String) headers.get(HL7Constants.HL7_SENDING_APPLICATION);
        String sendingFacility = (String) headers.get(HL7Constants.HL7_SENDING_FACILITY);
        if (!StringUtils.hasText(sendingFacility) || !StringUtils.hasText(sendingApp)) {
            throw new IllegalStateException("Device name not found");
        }
        return sendingFacility + "-" + sendingApp;
    }

    @Override
    public ObjectNode extractAttributeValues(Map<String, Object> headers) {
        ObjectNode on = objectMapper.createObjectNode();
        on.put("receivingApplication", (String) headers.get(HL7Constants.HL7_RECEIVING_APPLICATION));
        on.put("receivingFacility", (String) headers.get(HL7Constants.HL7_RECEIVING_FACILITY));
        return on;
    }

    @Override
    public List<TsValues> extractTelemetryValues(ORU_R01 oruMessage) {
        Map<LocalDateTime, List<OBX>> obxByObservationTime = oruR01Utils.getAllOBX(oruMessage).stream()
                .collect(groupingBy(oruR01Utils.getObxUtils()::getObservationDateTime));
        return obxByObservationTime.entrySet().stream().map(this::buildTsValueFromEntry).collect(Collectors.toList());
    }

    private TsValues buildTsValueFromEntry(Map.Entry<LocalDateTime, List<OBX>> entry) {
        LocalDateTime observationTime = entry.getKey();
        long observationTimeInMillis = observationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        ObjectNode values = objectMapper.createObjectNode();
        List<OBX> obxes = entry.getValue();
        obxes.forEach(obx -> {
            String key = oruR01Utils.getObxUtils().getObservationIdentifier(obx).getKey();
            Float value = oruR01Utils.getObxUtils().getValue(obx);
            values.put(key, value);
        });
        return TsValues.builder().ts(observationTimeInMillis).values(values).build();
    }

    public ACK buildAckFromHeaders(Map<String, Object> headers) {
        ACK ackMessage = new ACK();
        try {
            ackMessage.initQuickstart("ACK", (String) headers.get(HL7Constants.HL7_TRIGGER_EVENT), (String) headers.get(HL7Constants.HL7_PROCESSING_ID));
            ackMessage.getMSH().getSendingApplication().getHd1_NamespaceID().setValue((String) headers.get(HL7Constants.HL7_RECEIVING_APPLICATION));
            ackMessage.getMSH().getSendingFacility().getHd1_NamespaceID().setValue((String) headers.get(HL7Constants.HL7_RECEIVING_FACILITY));
            ackMessage.getMSH().getReceivingApplication().getHd1_NamespaceID().setValue((String) headers.get(HL7Constants.HL7_SENDING_APPLICATION));
            ackMessage.getMSH().getReceivingFacility().getHd1_NamespaceID().setValue((String) headers.get(HL7Constants.HL7_RECEIVING_FACILITY));
            ackMessage.getMSH().getMessageControlID().setValue((String) headers.get(HL7Constants.HL7_MESSAGE_CONTROL));
        } catch (Exception e) {
            log.error("", e);
        }
        return ackMessage;
    }

}
