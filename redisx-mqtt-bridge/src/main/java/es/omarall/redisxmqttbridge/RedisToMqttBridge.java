package es.omarall.redisxmqttbridge;

import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import es.omarall.redisxmqttbridge.model.BridgeDefinition;
import es.omarall.redisxmqttbridge.model.Direction;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class RedisToMqttBridge {

    @Bean
    public Completable fromRedisToMqttScenario(Mqtt5RxClient mqtt5RxClient, Flowable<Mqtt5Publish> fromRedisPublishable) {
        return mqtt5RxClient.publish(fromRedisPublishable)
                .doOnNext(publishResult -> log.debug(
                        "Publish acknowledged: {} - {}", publishResult.getPublish().getTopic(), new String(publishResult.getPublish().getPayloadAsBytes())))
                .doOnError(throwable -> log.error("Publish failed, {}", throwable.getMessage())).ignoreElements();
    }

    @Bean
    public Flowable<Mqtt5Publish> fromRedisPublishable(ReactiveRedisConnectionFactory connectionFactory, ApplicationProperties applicationProperties) {

        // Pick all Redis to MQTT bridge definitions
        List<BridgeDefinition> redisToMqttBridgeDefinitions = applicationProperties.getBridgeDefinitions().stream()
                .filter(bridgeDefinition -> bridgeDefinition.getDirection().equals(Direction.REDIS_TO_MQTT)).toList();

        // Map each bridge definition to its publishable stream ( )Flowable<Mqtt5Publish>)
        List<Flowable<Mqtt5Publish>> redisFlowables = redisToMqttBridgeDefinitions.stream()
                .map(bridgeDefinition -> redisFlowable(connectionFactory, bridgeDefinition.getStreamKey()).map(s -> Mqtt5Publish.builder()
                                .topic(bridgeDefinition.getTopic())
                                .payload(s.getBytes())
                                .build())
                        .doOnNext(m -> log.debug("Message on Bridge Definition: {} - xtream key: {} - Mqtt Topic: {}", bridgeDefinition.getName(),
                                bridgeDefinition.getStreamKey(), bridgeDefinition.getTopic())))
                .toList();

        // Finally, merge all publishable streams into one
        return Flowable.merge(redisFlowables);
    }


    /**
     * Flowable that reads from a given Redis Stream and emits the value of each published record
     */
    private Flowable<String> redisFlowable(ReactiveRedisConnectionFactory connectionFactory, String redisStreamKey) {
        // TODO: Add Consumer Group
        return Flowable.fromPublisher(StreamReceiver.create(connectionFactory, StreamReceiver.StreamReceiverOptions.builder()
                        .pollTimeout(Duration.ofMillis(500))
                        .targetType(String.class)
                        .build())
                .receive(StreamOffset.create(redisStreamKey, ReadOffset.lastConsumed()))
                .map(ObjectRecord::getValue));
    }
}
