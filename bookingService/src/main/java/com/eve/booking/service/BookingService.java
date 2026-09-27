package com.eve.booking.service;

import com.eve.booking.dto.BookingResponse;
import com.eve.booking.dto.CreateBookingRequest;
import com.eve.booking.entity.Booking;
import com.eve.booking.entity.CentreTest;
import com.eve.booking.enums.BookingStatus;
import com.eve.booking.exception.BookingNotFoundException;
import com.eve.booking.repository.BookingRepository;
import com.eve.booking.repository.CentreTestRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CentreTestRepository centreTestRepository;

    public BookingService(
            BookingRepository bookingRepository,
            CentreTestRepository centreTestRepository) {

        this.bookingRepository = bookingRepository;
        this.centreTestRepository = centreTestRepository;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {

        String userId = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        CentreTest centreTest = centreTestRepository
                .findByCentre_IdAndTest_Id(
                        request.centreId(),
                        request.testId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No test for centre "
                                        + request.centreId()
                                        + " and test "
                                        + request.testId()
                        )
                );

        Booking booking = Booking.builder()
                .userId(userId)
                .centre(centreTest.getCentre())
                .test(centreTest.getTest())
                .bookingDateTime(request.appointmentTime())
                .status(BookingStatus.PENDING)
                .amount(centreTest.getPrice())
                .build();

        Booking saved = bookingRepository.save(booking);

        return BookingResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() ->
                        new BookingNotFoundException(id)
                );

        return BookingResponse.fromEntity(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(
            String userId) {

        return bookingRepository
                .findByUserId(userId)
                .stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void updatePaymentStatus(
            Long bookingId,
            String paymentStatus) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(bookingId)
                );

        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {

            booking.setStatus(BookingStatus.CONFIRMED);

        } else if ("FAILED".equalsIgnoreCase(paymentStatus)) {

            booking.setStatus(BookingStatus.FAILED);

        } else {

            throw new IllegalArgumentException(
                    "Invalid payment status: " + paymentStatus
            );
        }

        bookingRepository.save(booking);
    }
}