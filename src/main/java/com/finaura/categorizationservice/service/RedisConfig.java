package com.finaura.categorizationservice.service;

import org.springframework.stereotype.Component;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;

@Component
public class RedisConfig {

    public Jedis createClient() {
        String host = System.getenv("REDIS_HOST");
        String password = System.getenv("REDIS_PASSWORD");
        int port = Integer.parseInt(System.getenv("REDIS_PORT"));

        DefaultJedisClientConfig config = DefaultJedisClientConfig.builder().password(password).ssl(true).build();
        
        return new Jedis(host,port,config);
    }
}