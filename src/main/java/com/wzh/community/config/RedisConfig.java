package com.wzh.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author wzh
 * @data 2022/8/10 -15:56
 */
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory); // 设置工厂才能访问数据库

        // key 序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // value 序列化
        template.setValueSerializer(RedisSerializer.json());
        // hash key序列化
        template.setHashKeySerializer(RedisSerializer.string());
        // hash value序列化
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }

}
