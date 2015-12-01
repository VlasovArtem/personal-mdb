package com.vlasovartem.pmdb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vlasovartem.pmdb.utils.serializer.LocalDateSerializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Created by artemvlasov on 29/11/15.
 */
@Configuration
@EnableWebMvc
@ComponentScan("com.vlasovartem.pmdb.controller")
public class ServletContextConfig extends WebMvcConfigurerAdapter {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder
                .json()
                .defaultViewInclusion(true)
                .autoDetectFields(true)
                .serializers(new LocalDateSerializer())
                .build();
        converters.add(new MappingJackson2HttpMessageConverter(mapper));
    }
}
