package es.omarall.thinsgboard.smartgw.connector.hl7;

import ca.uhn.hl7v2.model.v23.segment.OBR;
import es.omarall.thinsgboard.smartgw.connector.Constants;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OBRUtils {

    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);

    public String getID(OBR obr) {
        return obr.getSetIDObservationRequest().getValue();
    }

    public String getPlaceOrderNumber(OBR obr) {
        return obr.getPlacerOrderNumber(0).getEntityIdentifier().getValue();
    }

    public String getFillerOrderNumber(OBR obr) {
        return obr.getFillerOrderNumber().getEntityIdentifier().getValue();
    }

    public String getUniversalServiceText(OBR obr) {
        return obr.getUniversalServiceIdentifier().getIdentifier().getValue();
    }

    public LocalDateTime getObservationDateTime(OBR obr) {
        String observationDateTimeStr = obr.getObservationDateTime().getTimeOfAnEvent().getValue();
        return LocalDateTime.from(dateTimeFormatter.parse(observationDateTimeStr));
    }

    // TODO: Add more OBR fields
}
