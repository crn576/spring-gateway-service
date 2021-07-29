package com.sanbox.gatewayservice.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.Objects;

@Configuration
@EnableCaching
public class RedisCacheConfig {

  Logger logger = LoggerFactory.getLogger(RedisCacheConfig.class);

  @Autowired ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

  private final Integer tpsAllowed = 100;
  private final Integer burstRequestsAllowed = 100;
  private final Integer numberOfTokensPerRequest = 1;

  @Bean(name = "ratelimiter")
  RedisRateLimiter rateLimiter() {
    return new RedisRateLimiter(tpsAllowed, burstRequestsAllowed, numberOfTokensPerRequest);
  }

  @Bean("redisCFBean")
  @DependsOn(value = "redisServiceInstanceDetails")
  public LettuceConnectionFactory connectionFactory() {
    RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
    standaloneConfiguration.setHostName(getRedisInfo().getHostName());
    standaloneConfiguration.setPassword(getRedisInfo().getPassword());
    standaloneConfiguration.setPort(getRedisInfo().getPortNumber());
    return new LettuceConnectionFactory(standaloneConfiguration);
  }

  @Bean
  ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
      ReactiveRedisConnectionFactory factory) {
    return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
  }

  @Bean("redisServiceInstanceDetails")
  public RedisInstanceInfo getRedisInfo() {
    String vcap = System.getenv("VCAP_SERVICES");
    Gson gson = new Gson();
    JsonElement root = JsonParser.parseString(vcap);
    JsonObject redis = null;
    if (root != null) {
      if (root.getAsJsonObject().has("p.redis")) {
        redis = root.getAsJsonObject().get("p.redis").getAsJsonArray().get(0).getAsJsonObject();

      } else if (root.getAsJsonObject().has("p-redis")) {
        redis = root.getAsJsonObject().get("p-redis").getAsJsonArray().get(0).getAsJsonObject();
      } else {
        System.exit(0);
      }
    }

    if (redis != null) {
      JsonObject cred = redis.get("credentials").getAsJsonObject();
      RedisInstanceInfo instanceInfo = new RedisInstanceInfo();
      instanceInfo.setHostName(cred.get("host").getAsString());
      instanceInfo.setPortNumber(cred.get("port").getAsInt());
      instanceInfo.setPassword(cred.get("passowrd").getAsString());
      return instanceInfo;
    }

    return new RedisInstanceInfo();
  }

  public static class RedisInstanceInfo {
    private String hostName;
    private int portNumber;
    private String password;

    public String getHostName() {
      return hostName;
    }

    public void setHostName(String hostName) {
      this.hostName = hostName;
    }

    public int getPortNumber() {
      return portNumber;
    }

    public void setPortNumber(int portNumber) {
      this.portNumber = portNumber;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      RedisInstanceInfo that = (RedisInstanceInfo) o;
      return portNumber == that.portNumber
          && hostName.equals(that.hostName)
          && password.equals(that.password);
    }

    @Override
    public int hashCode() {
      return Objects.hash(hostName, portNumber, password);
    }
  }
}
