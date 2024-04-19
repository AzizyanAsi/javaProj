package net.idonow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class IdonowApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdonowApplication.class, args);
    }

}
