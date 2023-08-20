# Thingsboard SmartGateway Connector Starter

Tnis projects serves as the Spring Boot 3 starter project for the `thingsboard-smartgw-connector-base` library.

This starter simplifies the setup process, allowing the library to be imported and automatically configured within a
Spring Boot project.

## Steps:

1. Write a Configuration class providing those bean definitions

```java
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
```

2. A file `resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` that instructs
   Spring Boot about the Configuration classes to scan.

```text[org.springframework.boot.autoconfigure.AutoConfiguration.imports]
es.omarall.thingsboard.smartgw.connector.TbSmartGatewayConnectorAutoconfiguration
```
