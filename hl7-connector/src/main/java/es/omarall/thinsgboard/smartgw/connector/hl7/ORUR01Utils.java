package es.omarall.thinsgboard.smartgw.connector.hl7;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.OBR;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import ca.uhn.hl7v2.model.v23.segment.PID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Getter
public class ORUR01Utils {

    private final PIDUtils pidUtils;
    private final OBRUtils obrUtils;
    private final OBXUtils obxUtils;

    public PID getPID(ORU_R01 oruMessage) {
        return oruMessage.getRESPONSE().getPATIENT().getPID();
    }

    public OBR getOBR(ORU_R01 oruMessage) {
        return oruMessage.getRESPONSE().getORDER_OBSERVATION().getOBR();
    }

    public List<OBX> getAllOBX(ORU_R01 oruMessage) {
        try {
            return oruMessage.getRESPONSEAll().stream().flatMap(oruR01Response -> {
                        try {
                            if (oruR01Response.getORDER_OBSERVATIONAll() == null) {
                                return null;
                            }
                            return oruR01Response.getORDER_OBSERVATIONAll().stream();
                        } catch (HL7Exception e) {
                            return Stream.empty();
                        }
                    }).filter(Objects::nonNull)
                    .flatMap((ORU_R01_ORDER_OBSERVATION obs) -> {
                        try {
                            if (obs.getOBSERVATIONAll() == null) {
                                return null;
                            }
                            return obs.getOBSERVATIONAll().stream();
                        } catch (HL7Exception e) {
                            return Stream.empty();
                        }
                    }).filter(Objects::nonNull).map(ORU_R01_OBSERVATION::getOBX).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }
}
