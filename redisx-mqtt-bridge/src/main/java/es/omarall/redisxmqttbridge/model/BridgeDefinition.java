package es.omarall.redisxmqttbridge.model;

import lombok.Data;

/**
 * Connects a Redis stream to a MQTT topic in any direction. Either from Redis to MQTT or from MQTT to Redis, bytes will be seamlessly transferred.
 */
@Data
public class BridgeDefinition {

    /**
     * The bridge name
     */
    private String name;

    /**
     * The bridge direction either from Redis to MQTT or from MQTT to Redis
     */
    private Direction direction;

    /**
     * The redis stream key
     */
    private String streamKey;

    /**
     * The MQTT topic name
     */
    private String topic;
}
