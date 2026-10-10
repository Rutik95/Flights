package com.skybook;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// Run the JVM in UTC regardless of the host machine's timezone.
		// Also avoids Postgres rejecting legacy zone IDs like "Asia/Calcutta" on connect.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(BackendApplication.class, args);
	}

}
