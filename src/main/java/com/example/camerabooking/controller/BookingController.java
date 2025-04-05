package com.example.camerabooking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.camerabooking.model.Booking;
import com.example.camerabooking.model.BookingStatus;
import com.example.camerabooking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    /**
     * Get all bookings
     */
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        log.info("Fetching all bookings");
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get a booking by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        log.info("Fetching booking with ID: {}", id);
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new booking
     */
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        log.info("Creating a new booking: {}", booking);
        Booking newBooking = bookingService.createBooking(booking);
        return ResponseEntity.ok(newBooking);
    }

    /**
     * Update booking status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("Updating status for booking ID: {} to {}", id, status);
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            Booking updatedBooking = bookingService.updateBookingStatus(id, bookingStatus);
            return ResponseEntity.ok(updatedBooking);
        } catch (IllegalArgumentException e) {
            log.error("Invalid booking status: {}", status);
            return ResponseEntity.badRequest().body("Invalid booking status: " + status);
        }
    }

    /**
     * Delete a booking
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        log.info("Deleting booking with ID: {}", id);
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all bookings for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUser(@PathVariable Long userId) {
        log.info("Fetching bookings for user ID: {}", userId);
        List<Booking> userBookings = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(userBookings);
    }

    /**
     * Get all bookings by status (e.g., APPROVED, PENDING)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable BookingStatus status) {
        log.info("Fetching bookings with status: {}", status);
        List<Booking> statusBookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(statusBookings);
    }
}