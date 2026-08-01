package io.github.ladium1.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ErpApplication {

    static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
    }

}
