package com.example.camerabooking.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.camerabooking.model.Booking;
import com.example.camerabooking.model.BookingStatus;
import com.example.camerabooking.repository.BookingRepository;
import com.example.camerabooking.service.BookingService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
	
	@Autowired
    private  BookingRepository bookingRepository;

    @Override
    public List<Booking> getAllBookings() {
        log.info("Fetching all bookings");
        return bookingRepository.findAll();
    }

    @Override
    public Optional<Booking> getBookingById(Long id) {
        log.info("Fetching booking with ID: {}", id);
        return bookingRepository.findById(id);
    }

    @Override
    public Booking createBooking(Booking booking) {
        log.info("Creating new booking: {}", booking);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking updateBookingStatus(Long id, BookingStatus status) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            booking.setStatus(status);
            log.info("Updating booking ID {} to status {}", id, status);
            return bookingRepository.save(booking);
        } else {
            log.warn("Booking ID {} not found", id);
            return null;
        }
    }

    @Override
    public void deleteBooking(Long id) {
        log.warn("Deleting booking with ID: {}", id);
        bookingRepository.deleteById(id);
    }

    @Override
    public List<Booking> getBookingsByUser(Long userId) {
        log.info("Fetching bookings for user ID: {}", userId);
        return bookingRepository.findByUserId(userId);
    }

    @Override
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        log.info("Fetching bookings with status: {}", status);
        return bookingRepository.findByStatus(status);
    }

	
  
}
