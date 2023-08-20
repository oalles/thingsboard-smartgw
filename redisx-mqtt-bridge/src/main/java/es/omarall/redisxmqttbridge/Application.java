package es.omarall.redisxmqttbridge;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import io.reactivex.Completable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
@Slf4j
public class Application {

    @Value("${mqtt.host}")
    private String mqttHost;

    @Value("${mqtt.username}")
    private String mqttUsername;


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ApplicationRunner runner(Completable bridge) {
        return args -> bridge.blockingAwait();
    }

    @Bean
    public Completable bridge(Mqtt5RxClient mqtt5RxClient, Completable fromRedisToMqttScenario, Completable fromMqttToRedisScenario) {
        // As we use the reactive API, the following line does not connect yet, but returns a reactive type.
        Completable connectScenario = mqtt5RxClient.connect()
                .doOnSuccess(connAck -> log.info("MQTT client - Connected, {}", connAck.getReasonCode()))
                .doOnError(throwable -> log.error("MQTT client - Connection failed, {}", throwable.getMessage()))
                .retry()
                .ignoreElement();
        // As we use the reactive API, the following line does not disconnect yet, but returns a reactive type.
        Completable disconnectScenario = mqtt5RxClient.disconnect().doOnComplete(() -> log.warn("MQTT Client Disconnected"));

        // Reactive types can be easily and flexibly combined
        return connectScenario.andThen(fromRedisToMqttScenario.mergeWith(fromMqttToRedisScenario)).andThen(disconnectScenario);
    }

    @Bean
    public Mqtt5RxClient mqtt5RxClient() {
        return Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString()) // TODO: Add Client ID to properties
                .serverHost(mqttHost)
                .simpleAuth(Mqtt5SimpleAuth.builder()
                        .username(mqttUsername)
                        .build())
                .buildRx();
    }
}
