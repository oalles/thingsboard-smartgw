package es.omarall.thingsboard.smartgw.connector.base;

public interface DeviceNameExtractor<T> {

    String extractDeviceName(T t);
}
