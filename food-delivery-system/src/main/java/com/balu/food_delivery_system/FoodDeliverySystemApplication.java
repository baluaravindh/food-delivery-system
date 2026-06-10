package com.balu.food_delivery_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FoodDeliverySystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodDeliverySystemApplication.class, args);
	}

}
