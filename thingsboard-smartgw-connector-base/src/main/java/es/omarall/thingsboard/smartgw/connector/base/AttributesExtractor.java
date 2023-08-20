package es.omarall.thingsboard.smartgw.connector.base;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface AttributesExtractor<T> {
    ObjectNode extractAttributeValues(T t);
}
