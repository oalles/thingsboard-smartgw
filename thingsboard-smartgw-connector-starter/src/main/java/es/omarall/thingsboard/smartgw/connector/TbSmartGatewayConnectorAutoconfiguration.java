package es.omarall.thingsboard.smartgw.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.omarall.thingsboard.smartgw.connector.base.TbSmartGatewayProperties;
import es.omarall.thingsboard.smartgw.connector.base.TbSmartGatewayPublisher;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@AutoConfiguration
@EnableConfigurationProperties(TbSmartGatewayProperties.class)
public class TbSmartGatewayConnectorAutoconfiguration {

    @ConditionalOnMissingBean
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TbSmartGatewayPublisher tbSmartGatewayPublisher(TbSmartGatewayProperties properties, ObjectMapper objectMapper,
                                                           RedisTemplate<String, String> redisTemplate) {
        return new TbSmartGatewayPublisher(properties, objectMapper, redisTemplate);
    }

    @ConditionalOnMissingBean
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);
        return redisTemplate;
    }

    @ConditionalOnMissingBean
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        return objectMapper;
    }

}
