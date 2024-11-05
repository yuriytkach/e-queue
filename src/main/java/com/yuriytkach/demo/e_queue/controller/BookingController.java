package com.yuriytkach.demo.e_queue.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

import com.yuriytkach.demo.e_queue.config.AppProperties;
import com.yuriytkach.demo.e_queue.repository.BookingSlotRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BookingController {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  private final AppProperties appProperties;
  private final BookingSlotRepository bookingSlotRepository;
  private final RedisTemplate<String, String> redisTemplate;

  @GetMapping("/api/booking/status")
  public BookingWindowStatus getBookingStatus() {
    final long secondsForBooking = appProperties.getCycleDuration().toSeconds()
      - appProperties.getBookingRoundDuration().toSeconds();

    final long secondsSinceMidnight = LocalTime.now().toSecondOfDay();
    final long secondsInCurrentCycle = secondsSinceMidnight % appProperties.getCycleDuration().toSeconds();

    final boolean bookingOpen = secondsInCurrentCycle < (secondsForBooking - 5);

    final long secondsLeftInCurrentPhase;

    if (bookingOpen) {
      secondsLeftInCurrentPhase = secondsForBooking - secondsInCurrentCycle;
    } else {
      secondsLeftInCurrentPhase = appProperties.getCycleDuration().toSeconds() - secondsInCurrentCycle;
    }

    log.debug("Seconds left in current phase: {}. Booking open: {}", secondsLeftInCurrentPhase, bookingOpen);

    return new BookingWindowStatus(bookingOpen, secondsLeftInCurrentPhase);
  }

  @GetMapping("/api/booking/slots")
  public List<BookingSlotDto> getBookingSlots() {
    return StreamSupport.stream(bookingSlotRepository.findAll().spliterator(), false)
      .map(bookingSlot -> new BookingSlotDto(
        bookingSlot.id(),
        bookingSlot.startTime().format(TIME_FORMATTER),
        bookingSlot.endTime().toString())
      )
      .sorted(Comparator.comparing(BookingSlotDto::startTime))
      .toList();
  }

  @PostMapping("/api/booking/book")
  public ResponseEntity<?> bookSlot(@RequestParam String slotId, @AuthenticationPrincipal OAuth2User oauthUser) {
    final String userId = oauthUser.getAttribute("id").toString();

    // Check if the user has already booked
    final Boolean hasBooked = redisTemplate.opsForSet().isMember("booked_users", userId);
    if (Boolean.TRUE.equals(hasBooked)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You have already booked a slot in this window.");
    }
    // Add the user to the set of booked users
    redisTemplate.opsForSet().add("booked_users", userId);

    if (redisTemplate.hasKey("winner:" + userId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You have already won a slot.");
    }

    // Save the user's selection for the slot
    final String slotUsersKey = "slot:" + slotId + ":users";
    redisTemplate.opsForSet().add(slotUsersKey, userId);

    log.info("User {} has selected slot {}", userId, slotId);

    return ResponseEntity.ok("Selection received");
  }

  @GetMapping("/api/booking/winner")
  public ResponseEntity<?> isWinner(@AuthenticationPrincipal OAuth2User oauthUser) {
    final String userId = oauthUser.getAttribute("id").toString();

    // Check in Redis if user is a winner
    final String winnerKey = "winner:" + userId;
    final String slotId = redisTemplate.opsForValue().get(winnerKey);

    if (slotId != null) {
      return ResponseEntity.ok(Map.of("slotId", slotId));
    } else {
      return ResponseEntity.ok(Map.of());
    }
  }

  @GetMapping("/api/booking/hasBooked")
  public ResponseEntity<?> hasUserBooked(@AuthenticationPrincipal final OAuth2User oauthUser) {
    final String userId = oauthUser.getAttribute("id").toString();

    final Boolean hasBooked = redisTemplate.opsForSet().isMember("booked_users", userId);
    final Map<String, Boolean> response = Map.of("hasBooked", Boolean.TRUE.equals(hasBooked));
    return ResponseEntity.ok(response);
  }

  public record BookingSlotDto(String id, String startTime, String endTime) { }

  public record BookingWindowStatus(
    boolean bookingOpen,
    long secondsLeftInCurrentPhase
  ) { }

}
