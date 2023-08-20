package es.omarall.thingsboard.smartgw.connector.base;

public interface DeviceTypeExtractor<T> {

    String extractDeviceType(T t);
}
