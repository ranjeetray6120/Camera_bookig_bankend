package com.example.camerabooking.service;


import java.util.List;
import java.util.Optional;

import com.example.camerabooking.model.Booking;
import com.example.camerabooking.model.BookingStatus;

public interface BookingService {

    List<Booking> getAllBookings();

    Optional<Booking> getBookingById(Long id);

    Booking createBooking(Booking booking);

    Booking updateBookingStatus(Long id, BookingStatus status);

    void deleteBooking(Long id);

    List<Booking> getBookingsByUser(Long userId);

    List<Booking> getBookingsByStatus(BookingStatus status);
    
}
