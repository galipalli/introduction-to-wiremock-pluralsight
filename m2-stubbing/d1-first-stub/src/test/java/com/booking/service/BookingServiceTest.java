package com.booking.service;

import com.booking.domain.BookingPayment;
import com.booking.domain.CreditCard;
import com.booking.gateway.PayBuddyGateway;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.booking.service.BookingResponse.BookingResponseStatus.COMPLETE;
import static com.booking.service.BookingResponse.BookingResponseStatus.REJECTED;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingServiceTest {

    private BookingService bookingService;

    @Before
    public void setUp() {
        bookingService = new BookingService(new PayBuddyGateway("http://paybuddy-pluralsight"));
    }

    @Test
    public void shouldPayForBookingSuccessfully() {
        // When
        final BookingResponse bookingResponse = bookingService.payForBooking(
                new BookingPayment(
                        "1111",
                        new BigDecimal("20.55"),
                        new CreditCard("1234-1234-1234-1234",
                                LocalDate.of(2018, 2, 1))));

        // Then
        assertThat(bookingResponse)
                .isEqualTo(new BookingResponse("1111", "2222", COMPLETE));
    }

    @Test
    public void shouldFailToPayForBooking() {
        // When
        final BookingResponse bookingResponse = bookingService.payForBooking(
                new BookingPayment(
                        "1111",
                        new BigDecimal("20.55"),
                        new CreditCard("1234-1234-1234-1234",
                                LocalDate.of(2018, 2, 1))));

        // Then
        assertThat(bookingResponse)
                .isEqualTo(new BookingResponse("1111", "7777", REJECTED));
    }

}
