package com.hahasoft.poc.observability;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
public class PocObservabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocObservabilityApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}

	@RestController
	class HelloController {

		private final RestTemplate restTemplate;
		private final SleepService sleepService;

		HelloController(RestTemplate restTemplate, SleepService sleepService) {
			this.restTemplate = restTemplate;
			this.sleepService = sleepService;
		}

		@GetMapping("/hello")
		public String hello() {
			ResponseEntity<String> responseEntity = this.restTemplate.postForEntity("https://httpbin.org/post", "Hello, Cloud!", String.class);
			MDC.put("test", ""+ System.currentTimeMillis());
			log.info("response: {}", responseEntity.getBody());
			return responseEntity.getBody();
		}

		@GetMapping("/exception")
		public String exception() {
			throw new IllegalArgumentException("This id is invalid");
		}

		@GetMapping("/sleep")
		public Long sleep(@RequestParam Long ms) {
			Long result = this.sleepService.doSleep(ms);
			return result;
		}
	}

	@Service
	class SleepService {
		@Timed(value = "do.sleep.method.timed")
		public Long doSleep(Long ms) {
			try {
				TimeUnit.MILLISECONDS.sleep(ms);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return ms;
		}
	}

}
