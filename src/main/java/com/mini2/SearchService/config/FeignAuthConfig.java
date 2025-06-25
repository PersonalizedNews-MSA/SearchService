package com.mini2.SearchService.config;



import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor feignHeaderPropagatingInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

                if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
                    HttpServletRequest request = servletRequestAttributes.getRequest();

                    String authorizationHeader = request.getHeader("Authorization");
                    String userIdHeader = request.getHeader("X-Auth-userId");

                    if (authorizationHeader != null) {
                        template.header("Authorization", authorizationHeader);
                    }

                    if (userIdHeader != null) {
                        template.header("X-Auth-userId", userIdHeader);
                    }
                }
            }
        };
    }
}
