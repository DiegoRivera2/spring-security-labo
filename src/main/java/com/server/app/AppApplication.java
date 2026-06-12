package com.server.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(AppApplication.class, args);
	}

	private static void loadDotEnv() {
		Path envFile = Path.of(".env");
		if (!Files.exists(envFile)) {
			return;
		}
		try {
			Files.lines(envFile)
					.filter(line -> !line.isBlank() && !line.startsWith("#"))
					.forEach(line -> {
						int idx = line.indexOf('=');
						if (idx > 0) {
							String key = line.substring(0, idx).trim();
							String value = line.substring(idx + 1).trim();
							if (System.getenv(key) == null) {
								System.setProperty(key, value);
							}
						}
					});
		} catch (IOException ignored) {
		}
	}
}
