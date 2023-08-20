package es.omarall.thingsboard.smartgw.connector.base;

import java.util.List;

public interface TelemetryValuesExtractor<T> {
    List<TsValues> extractTelemetryValues(T t);
}
