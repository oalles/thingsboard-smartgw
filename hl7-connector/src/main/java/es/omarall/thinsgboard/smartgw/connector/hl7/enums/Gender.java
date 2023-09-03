package es.omarall.thinsgboard.smartgw.connector.hl7.enums;

import lombok.Getter;

@Getter
public enum Gender {
    MALE("M"), FEMALE("F"), NON_BINARY("NB");

    private final String code;

    Gender(String code) {
        this.code = code;
    }

    public static Gender of(String code) {
        if (code == null) {
            return null;
        } else if (code.equals("M")) {
            return Gender.MALE;
        } else if (code.equals("F")) {
            return Gender.FEMALE;
        } else {
            return Gender.NON_BINARY;
        }
    }

}
