package com.hahasoft.poc.observability;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.annotation.NewSpan;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

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
		private final ObservationRegistry observationRegistry;

		HelloController(RestTemplate restTemplate, SleepService sleepService,ObservationRegistry observationRegistry) {
			this.restTemplate = restTemplate;
			this.sleepService = sleepService;
			this.observationRegistry = observationRegistry;

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
			Long result = Observation.createNotStarted("do.sleep.method.timed", this.observationRegistry) // metric name
					.contextualName("do-sleep-method-span") // span name
					.lowCardinalityKeyValue("low", "low") // tags for both metric and span
					.highCardinalityKeyValue("high", "high") // tgs for span
					.observe(() -> this.sleepService.doSleep(ms) );

//			Long result = this.sleepService.doSleep(ms);
			return result;
		}
	}

	@Service
	class SleepService {
//		@Timed(value = "do.sleep.method.timed")
//		@NewSpan(value = "do-sleep-method-span")
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
