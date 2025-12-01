package com.fleetsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Shivam Srivastav
 */
@SpringBootApplication
@EnableScheduling
public class FleetSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetSyncApplication.class, args);
    }

}
