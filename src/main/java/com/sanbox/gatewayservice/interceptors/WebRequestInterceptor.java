package com.sanbox.gatewayservice.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class WebRequestInterceptor implements WebFilter {

    Logger logger = LoggerFactory.getLogger(WebRequestInterceptor.class);

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        logger.info(" Path="+ serverWebExchange.getRequest().getPath());
        return webFilterChain.filter(serverWebExchange);
    }
}
