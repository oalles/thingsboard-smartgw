package es.omarall.thinsgboard.smartgw.connector.hl7.enums;

import es.omarall.thinsgboard.smartgw.connector.Constants;
import lombok.Getter;

@Getter
public enum Units {
    RESPIRATORY_RATE(Constants.RESPIRATORY_RATE_UNIT), HEART_RATE(Constants.HEART_RATE_UNIT), BODY_TEMPERATURE(Constants.BODY_TEMPERATURE_UNIT);

    private final String unit;

    Units(String unit) {
        this.unit = unit;
    }

    public static Units of(String unit) {
        if (unit == null) {
            return null;
        } else if (unit.equals(Constants.RESPIRATORY_RATE_UNIT)) {
            return Units.RESPIRATORY_RATE;
        } else if (unit.equals(Constants.HEART_RATE_UNIT)) {
            return Units.HEART_RATE;
        } else if (unit.equals(Constants.BODY_TEMPERATURE_UNIT)) {
            return Units.BODY_TEMPERATURE;
        } else {
            return null;
        }
    }

}
