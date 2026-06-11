package my.maleva.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MalevaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MalevaApplication.class, args);
	}

}
