package com.example.camerabooking.repository;

import com.example.camerabooking.model.Booking;
import com.example.camerabooking.model.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find bookings by user ID
    List<Booking> findByUserId(Long userId);

    // Find bookings by status (PENDING, APPROVED, REJECTED)
    List<Booking> findByStatus(BookingStatus status);

    // Find bookings by event type (WEDDING, ANNIVERSARY, BIRTHDAY)
    List<Booking> findByEventType(String eventType);
    
   
  
}
