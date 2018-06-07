package com.ztdx.eams;

import com.ztdx.eams.basic.utils.FileReaderUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.File;

@SpringBootApplication
@EnableJpaRepositories
@ComponentScan(value = "com.ztdx.eams")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
