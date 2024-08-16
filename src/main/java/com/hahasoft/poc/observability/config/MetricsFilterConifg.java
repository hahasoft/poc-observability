package com.hahasoft.poc.observability.config;
import io.micrometer.core.instrument.Tag;
import java.time.ZoneId;
import java.util.UUID;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MetricsFilterConifg {

//    @Bean
//    public MeterFilter commonTagsMeterFilter() {
//        return MeterFilter.commonTags(
//                List.of(
//                        Tag.of("instance.uuid", UUID.randomUUID().toString()),
//                        Tag.of("zone.id", ZoneId.of("Europe/Berlin").toString())
//                )
//        );
//    }
}
