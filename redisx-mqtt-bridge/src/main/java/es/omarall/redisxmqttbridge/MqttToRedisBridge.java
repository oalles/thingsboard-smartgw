package es.omarall.redisxmqttbridge;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import es.omarall.redisxmqttbridge.model.BridgeDefinition;
import es.omarall.redisxmqttbridge.model.Direction;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MqttToRedisBridge {

    @Bean
    public Completable fromMqttToRedisScenario(ApplicationProperties applicationProperties, Mqtt5RxClient mqtt5RxClient,
                                               final ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {

        // Pick all MQTT to Redis bridge definitions
        List<BridgeDefinition> mqttToRedisBridgeDefinitions = applicationProperties.getBridgeDefinitions().stream()
                .filter(bridgeDefinition -> Direction.MQTT_TO_REDIS.equals(bridgeDefinition.getDirection())).toList();

        // Build MQTT Subscriptions
        List<Mqtt5Subscription> mqtt5Subscriptions = mqttToRedisBridgeDefinitions.stream().map(BridgeDefinition::getTopic).map(topic -> Mqtt5Subscription.builder()
                .topicFilter(topic).qos(MqttQos.AT_MOST_ONCE).build()).toList(); // TODO: Externalize QoS

        // Build the flowable that will emit the messages
        Flowable<Mqtt5Publish> subscriptions = mqtt5RxClient.subscribePublishesWith()
                .addSubscriptions(mqtt5Subscriptions).applySubscribe()
                .doOnSingle(subAck -> log.info("Subscribed to: {} - Reason codes: {}",
                        mqttToRedisBridgeDefinitions.stream().map(BridgeDefinition::getTopic).collect(Collectors.joining()),
                        subAck.getReasonCodes()))
                .doOnError(throwable -> log.error("Subscription failed, {}", throwable.getMessage()))
                .doOnNext(mqtt5Publish -> log.info("Received message on topic: {} - Payload: {}",
                        mqtt5Publish.getTopic(), new String(mqtt5Publish.getPayloadAsBytes())));


        // Send the messages to Redis
        Flowable<RecordId> redords = subscriptions.flatMap(mqtt5Message -> {
            String mqtt5MessageTopicName = mqtt5Message.getTopic().toString();
            Optional<String> streamKey = mqttToRedisBridgeDefinitions.stream()
                    .filter(bridgeDefinition -> bridgeDefinition.getTopic().equals(mqtt5MessageTopicName))
                    .map(BridgeDefinition::getStreamKey).findFirst();
            if (streamKey.isPresent()) {
                String uniqueId = UUID.randomUUID().toString();
                String payload = new String(mqtt5Message.getPayloadAsBytes());
                return reactiveRedisTemplate.opsForStream().add(StreamRecords.newRecord().in(streamKey.get()).ofMap(Collections.singletonMap(uniqueId, payload)));
            } else {
                log.debug("No stream key found for topic {}", mqtt5Message.getTopic());
                return Mono.empty();
            }
        });

        return redords.ignoreElements();
    }
}
