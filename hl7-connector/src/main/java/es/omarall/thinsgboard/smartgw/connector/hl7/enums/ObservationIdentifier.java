package es.omarall.thinsgboard.smartgw.connector.hl7.enums;

import es.omarall.thinsgboard.smartgw.connector.Constants;

public enum ObservationIdentifier {
    RESPIRATORY_RATE(Constants.RESPIRATORY_RATE_LOINC_CODE, "breath_rate"), HEART_RATE(Constants.HEART_RATE_LOINC_CODE, "heart_rate"), BODY_TEMPERATURE(Constants.BODY_TEMPERATURE_LOINC_CODE, "body_temp");

    private final String code;
    private final String key;

    ObservationIdentifier(String loincCode, String key) {
        this.code = loincCode;
        this.key = key;
    }

    public static ObservationIdentifier of(String code) {
        if (code == null) {
            return null;
        } else if (code.equals(Constants.RESPIRATORY_RATE_LOINC_CODE)) {
            return ObservationIdentifier.RESPIRATORY_RATE;
        } else if (code.equals(Constants.HEART_RATE_LOINC_CODE)) {
            return ObservationIdentifier.HEART_RATE;
        } else if (code.equals(Constants.BODY_TEMPERATURE_LOINC_CODE)) {
            return ObservationIdentifier.BODY_TEMPERATURE;
        } else {
            return null;
        }
    }

    public String getCode() {
        return code;
    }

    public String getKey() {
        return key;
    }
}
