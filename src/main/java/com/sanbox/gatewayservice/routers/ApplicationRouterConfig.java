package com.sanbox.gatewayservice.routers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class ApplicationRouterConfig {

  @Autowired
  @Qualifier("ratelimiter")
  RedisRateLimiter rateLimiter;


  /**
   *  Sample Route setup for allowing only 100 requests per second.
   *
   * @param builder
   * @return
   */

  @Bean
  public RouteLocator routeLocator(RouteLocatorBuilder builder) {
    return builder
        .routes()
        .route(
            "route-1",
            rs ->
                rs.path("/test")
                    .and()
                    .method("GET")
                    .filters(
                        fs ->
                            fs.requestRateLimiter(
                                rl ->
                                    rl.setRateLimiter(rateLimiter)
                                        .setKeyResolver(exchange -> Mono.just("/test"))))
                    .uri(" URL for downstream Micro Service"))
        .build();
  }
}
