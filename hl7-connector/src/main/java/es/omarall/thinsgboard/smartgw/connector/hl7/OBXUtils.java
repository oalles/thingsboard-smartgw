package es.omarall.thinsgboard.smartgw.connector.hl7;

import ca.uhn.hl7v2.model.v23.segment.OBX;
import es.omarall.thinsgboard.smartgw.connector.Constants;
import es.omarall.thinsgboard.smartgw.connector.hl7.enums.ObservationIdentifier;
import es.omarall.thinsgboard.smartgw.connector.hl7.enums.Units;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OBXUtils {

    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);

    public String getID(OBX obx) {
        return obx.getSetIDOBX().getValue();
    }

    public String getObservationSubId(OBX obx) {
        return obx.getObservationSubID().getValue();
    }

    public String getValueType(OBX obx) {
        return obx.getValueType().getValue();
    }

    public Float getValue(OBX obx) {
        return Float.parseFloat(obx.getObservationValue(0).getData().toString());
    }

    public LocalDateTime getObservationDateTime(OBX obx) {
        return LocalDateTime.from(dateTimeFormatter.parse(obx.getObx14_DateTimeOfTheObservation().getTimeOfAnEvent().getValue()));
    }

    public boolean isAbnormal(OBX obx) {
        return obx.getAbnormalFlags(0).getValue() != null && obx.getAbnormalFlags(0).getValue().equals(Constants.ABNORMAL_VALUE_FLAG);
    }

    public Units getUnits(OBX obx) {
        return Units.of(obx.getUnits().getIdentifier().getValue());
    }

    public ObservationIdentifier getObservationIdentifier(OBX obx) {
        return ObservationIdentifier.of(obx.getObservationIdentifier().getIdentifier().getValue());
    }

    // TODO: Add more OBX fields
}
