package com.ztdx.eams.basic.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ztdx.eams.basic.Interceptor;
import com.ztdx.eams.basic.params.JsonParamResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.io.IOException;
import java.util.List;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    private final Interceptor interceptor;
    private final ObjectMapper jsonMapper;

    @Autowired
    public WebConfig(Interceptor interceptor,ObjectMapper jsonMapper) {
        this.interceptor = interceptor;
        this.jsonMapper = jsonMapper;
    }

    /**
     * URL拦截配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }


    /**
     * 跨域访问配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowCredentials(true)
                .allowedMethods("POST", "GET", "PUT", "DELETE")
                .allowedHeaders("*");
    }

    /**
     * 页面模板配置
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/");
    }

    /**
     * 注册参数解析器
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        argumentResolvers.add(new JsonParamResolver(jsonMapper));
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter() {
            protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
                generator.writeRaw("{\"data\":");
                super.writePrefix(generator, object);
            }

            protected void writeSuffix(JsonGenerator generator, Object object) throws IOException {
                generator.writeRaw("}");
                super.writeSuffix(generator, object);
            }
        };
        mappingJackson2HttpMessageConverter.setObjectMapper(jsonMapper);
        return  mappingJackson2HttpMessageConverter;
    }

    @Bean
    public ExpressionParser getExpressionParser(){
        return new SpelExpressionParser();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJackson2HttpMessageConverter());
        super.addDefaultHttpMessageConverters(converters);
    }
}
