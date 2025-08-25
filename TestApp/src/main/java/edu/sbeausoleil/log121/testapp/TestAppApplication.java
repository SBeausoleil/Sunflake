package edu.sbeausoleil.log121.testapp;

import com.sb.flake.SunflakeJpaContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestAppApplication {

    @Autowired
    private CoffeeRepository coffeeRepository;

    public static void main(String[] args) {
        SpringApplication.run(TestAppApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            var coffee = new Coffee("My Coffee");
            coffeeRepository.save(coffee);
        };
    }

}
