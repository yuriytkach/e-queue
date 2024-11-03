package com.yuriytkach.demo.e_queue.service;

import java.time.LocalTime;

import com.yuriytkach.demo.e_queue.entity.BookingSlot;
import com.yuriytkach.demo.e_queue.repository.BookingSlotRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

  private final BookingSlotRepository bookingSlotRepository;
  private final StringRedisTemplate redisTemplate;

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    initializeBookingSlots();
  }

  @Scheduled(cron = "${app.reset-cron}")
  public void fullReset() {
    bookingSlotRepository.deleteAll();
    redisTemplate.delete("booking_initialized");
    redisTemplate.delete("booked_users");
    redisTemplate.keys("winner:*").forEach(winner -> {
      log.debug("Winner in previous grand cycle: {} - {}", winner, redisTemplate.opsForValue().get(winner));
      redisTemplate.delete(winner);
    });
    log.info("Full reset have been completed");
    initializeBookingSlots();
  }

  public void initializeBookingSlots() {
    if (!isBookingInitialized()) {
      final LocalTime startTime = LocalTime.of(8, 0);
      final LocalTime endTime = LocalTime.of(20, 0);

      for (LocalTime time = startTime; time.isBefore(endTime); time = time.plusMinutes(30)) {
        final BookingSlot bookingSlot = new BookingSlot(time.toString(), time, time.plusMinutes(30));
        bookingSlotRepository.save(bookingSlot);
      }

      redisTemplate.opsForValue().set("booking_initialized", "true");

      log.info("Booking slots have been initialized");
    } else {
      log.info("Booking slots have already been initialized");
    }
  }

  private boolean isBookingInitialized() {
    return redisTemplate.hasKey("booking_initialized");
  }

}
