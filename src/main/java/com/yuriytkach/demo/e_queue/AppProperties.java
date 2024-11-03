package com.yuriytkach.demo.e_queue;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("app")
public class AppProperties {

  private Duration cycleDuration;
  private Duration bookingRoundDuration;

}
