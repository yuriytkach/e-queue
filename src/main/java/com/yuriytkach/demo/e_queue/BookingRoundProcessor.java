package com.yuriytkach.demo.e_queue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingRoundProcessor {

  private final RedisTemplate<String, String> redisTemplate;
  private final BookingSlotRepository bookingSlotRepository;

  @Scheduled(cron = "${app.booking-cron}")
  public void processBookingRound() {
    final Boolean lockAcquired = redisTemplate.opsForValue()
      .setIfAbsent("bookingRoundLock", "locked", Duration.ofSeconds(10));

    if (Boolean.TRUE.equals(lockAcquired)) {
      try {
        log.info("Processing booking round");

        // Fetch all booking slots
        final Iterable<BookingSlot> slots = bookingSlotRepository.findAll();

        for (BookingSlot slot : slots) {
          final String slotUsersKey = "slot:" + slot.id() + ":users";
          final Set<String> userIds = redisTemplate.opsForSet().members(slotUsersKey);

          if (userIds == null || userIds.isEmpty()) {
            log.trace("No users for slot: {}", slot.id());
          } else {
            log.debug("Processing slot: {} with {} users", slot.id(), userIds.size());
            // Randomly select a winner
            final List<String> userIdList = new ArrayList<>(userIds);
            Collections.shuffle(userIdList);
            final String winnerUserId = userIdList.getFirst();

            log.info("Winner for slot {}: {}", slot.id(), winnerUserId);

            // Save the winner information in Redis
            final String winnerKey = "winner:" + winnerUserId;
            redisTemplate.opsForValue().set(winnerKey, slot.id());

            // Remove the slot from available slots
            bookingSlotRepository.deleteById(slot.id());

            // Remove the slot's users list from Redis
            redisTemplate.delete(slotUsersKey);
          }
        }
        redisTemplate.delete("booked_users"); // remove booked users to allow in new round
      } finally {
        redisTemplate.delete("bookingRoundLock");
      }
    } else {
      log.info("Booking round is already in progress");
    }
  }

}
