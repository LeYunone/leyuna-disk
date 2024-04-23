package com.leyunone.disk.system.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.time.Duration;

/**
 * @author LeYunone
 * @email 365627310@qq.com
 * @create 2021-08-31 16:31
 *  redis 配置
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.poolMaxIdle}")
    private int maxIdle;

    @Value("${spring.redis.poolMaxWait}")
    private long maxWaitMillis;

    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory){
        StringRedisTemplate template = new StringRedisTemplate(factory);
        setSerializer(template);//设置序列化工具
        template.afterPropertiesSet();
        return template;
    }
    private void setSerializer(StringRedisTemplate template){
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
    }

//    @Bean
//    public RedisCacheManager setupRedisCacheManager(RedisProperties redisProperties,
//                                                    RedisConnectionFactory redisConnectionFactory) {
//        ObjectMapper mapper = new ObjectMapper();
//        // 支持解析时间里面带T的格式
//        mapper.registerModule(new JavaTimeModule());
//
//        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
//                // 设置超时时间
//                .entryTtl(Duration.ofHours(12))
//                // 禁止缓存null值
//                .disableCachingNullValues();
//
//        return RedisCacheManager.builder(redisConnectionFactory)
//                .cacheDefaults(redisCacheConfiguration).transactionAware().build();
//    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
