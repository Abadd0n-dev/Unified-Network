package com.unifiedNetwork.UnifiedNetwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.unifiedNetwork.UnifiedNetwork"})
@EnableJpaRepositories(basePackages = "com.unifiedNetwork.UnifiedNetwork.repository")
public class UnifiedNetworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnifiedNetworkApplication.class, args);
	}

}
