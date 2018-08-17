package com.ztdx.eams.basic.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ztdx.eams.basic.CustomErrorController;
import com.ztdx.eams.basic.Interceptor;
import com.ztdx.eams.basic.params.JsonParamResolver;
import org.apache.commons.lang.StringEscapeUtils;
import org.quartz.SchedulerConfigException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.quartz.SimpleThreadPoolTaskExecutor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@EnableAsync
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    private final Interceptor interceptor;
    private final ObjectMapper jsonMapper;
    private final ServerProperties serverProperties;

    @Autowired
    public WebConfig(Interceptor interceptor, ObjectMapper jsonMapper, ServerProperties serverProperties) {
        this.interceptor = interceptor;
        this.jsonMapper = jsonMapper;
        this.serverProperties = serverProperties;
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
                StringBuilder response = new StringBuilder();
                Map map = null;
                if (object instanceof Map){
                    map = (Map)object;
                }

                response.append("{\"timestamp\":");
                response.append(System.currentTimeMillis());
                response.append(",\"code\":");
                response.append((map == null || map.get("system_code") == null) ? 200 : map.get("system_code"));
                response.append(",\"status\":");
                response.append(map == null || map.get("system_status") == null ? 200 : map.get("system_status"));
                response.append(",\"message\":\"");
                response.append(format(map, "system_message", ""));
                response.append("\",\"path\":\"");
                response.append(format(map, "system_path", ""));
                response.append("\",\"trace\":\"");
                response.append(format(map, "system_trace", ""));
                response.append("\",\"exception\":\"");
                response.append(format(map, "system_exception", ""));
                response.append("\",\"data\":");

                generator.writeRaw(response.toString());
                if (map != null && map.get("system_code") != null && map.get("system_status") != null && map.get("system_message") != null){
                    map.clear();
                }
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

    private Object format(Map map, String field, Object def){
        String result;
        if (map == null || map.get(field) == null ){
            result = def.toString();
        }else{
            result = map.get(field).toString();
        }
        return StringEscapeUtils.escapeJava(result);
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

    @Bean
    public CustomErrorController basicErrorController(ErrorAttributes errorAttributes) {
        return new CustomErrorController(errorAttributes, this.serverProperties.getError());
    }

    @Bean
    public TaskExecutor taskExecutor() throws SchedulerConfigException {
        SimpleThreadPoolTaskExecutor taskExecutor = new SimpleThreadPoolTaskExecutor();
        taskExecutor.setThreadCount(1);
        taskExecutor.setThreadPriority(5);
        taskExecutor.setThreadNamePrefix("自定义线程名");
        taskExecutor.initialize();
        return taskExecutor;
    }
}
