package es.omarall.thinsgboard.smartgw.connector.hl7;

import ca.uhn.hl7v2.model.v23.segment.PID;
import es.omarall.thinsgboard.smartgw.connector.Constants;
import es.omarall.thinsgboard.smartgw.connector.hl7.enums.Gender;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class PIDUtils {

    public final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);

    // Access individual fields of the PID segment
    public String getPatientID(PID pid) {
        return pid.getPid1_SetIDPatientID().getValue();
    }

    public String getPatientExternalID(PID pid) {
        return pid.getPid2_PatientIDExternalID().getID().getValue();
    }

    public String getPatientName(PID pid) {
        String patientName = null;
        if (pid.getPatientNameReps() > 0) {
            patientName = Arrays.stream(pid.getPatientName()).map(pn -> pn.getGivenName().getValue() + " " + pn.getFamilyName().getValue()).collect(Collectors.joining(","));
        }
        return patientName;
    }

    public LocalDate getDateOfBirth(PID pid) {
        String dobStr = pid.getPid7_DateOfBirth().getTimeOfAnEvent().getValue();
        if (dobStr != null) {
            return LocalDate.from(dateFormatter.parse(dobStr));
        }
        return null;
    }

    public Gender getGender(PID pid) {
        String genderStr = pid.getPid8_Sex().getValue();
        return Gender.of(genderStr);
        // TODO: Add more PID fields
    }
}
