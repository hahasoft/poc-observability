package com.hahasoft.poc.observability;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PocObservabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocObservabilityApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}


	@RestController
	class HelloController {

		private final RestTemplate restTemplate;

		HelloController(RestTemplate restTemplate) {
			this.restTemplate = restTemplate;
		}

		@GetMapping("/hello")
		public String hello() {
			ResponseEntity<String> responseEntity = this.restTemplate.postForEntity("https://httpbin.org/post", "Hello, Cloud!", String.class);
			return responseEntity.getBody();
		}
	}

}
