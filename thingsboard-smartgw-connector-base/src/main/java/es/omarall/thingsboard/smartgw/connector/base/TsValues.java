package es.omarall.thingsboard.smartgw.connector.base;


import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TsValues {
    private long ts;
    private ObjectNode values;
}
