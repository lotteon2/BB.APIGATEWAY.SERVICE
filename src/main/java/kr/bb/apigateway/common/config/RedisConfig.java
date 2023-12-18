package kr.bb.apigateway.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

  @Bean
  public LettuceConnectionFactory connectionFactory() {
    return new LettuceConnectionFactory();
  }

  @Bean
  public RedisTemplate<String, Object> deliveryRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    return template;
  }




}