package com.works.configs;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class AppBeans {

    @Primary // ÇÖZÜM BURADA: Spring artık varsayılan olarak bunu seçecek
    @Bean(name = "model")
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper;
    }
}