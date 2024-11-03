package com.yuriytkach.demo.e_queue.entity;

import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("BookingSlot")
public record BookingSlot(
  @Id
  String id,
  LocalTime startTime,
  LocalTime endTime
) {

}
