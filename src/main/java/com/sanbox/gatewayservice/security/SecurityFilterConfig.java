package com.sanbox.gatewayservice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.web.server.MatcherSecurityWebFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityFilterConfig {

  Logger logger = LoggerFactory.getLogger(SecurityFilterConfig.class);

  /***
   *  Replace dummy resource, and token issuers
   *
   *
   */
  private String resource = "myresource";

  private String eastDataCenterTokenIssuer = "https://eastdc.com/oauth/token";
  private String westDataCenterTokenIssuer = "https://westdc.com/oauth/token";

  /*
   * Customise the Security Filter Chain.
   * */

  @Bean
  public SecurityWebFilterChain configure(ServerHttpSecurity http) {
    return http.authorizeExchange()
        .pathMatchers("/*")
        .hasAuthority("SCOPE_" + resource + ".resource")
        .anyExchange()
        .authenticated()
        .and()
        .csrf()
        .disable()
        .oauth2ResourceServer(
            tokenValidator ->
                tokenValidator.authenticationManagerResolver(
                    new JwtIssuerReactiveAuthenticationManagerResolver(
                        eastDataCenterTokenIssuer, westDataCenterTokenIssuer)))
        .cors()
        .disable()
        .httpBasic()
        .disable()
        .build();
  }
}
