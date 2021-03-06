package com.booking.service;

import com.booking.domain.BookingPayment;
import com.booking.domain.CreditCard;
import com.booking.gateway.PayBuddyGateway;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.booking.service.BookingResponse.BookingResponseStatus.COMPLETE;
import static com.booking.service.BookingResponse.BookingResponseStatus.REJECTED;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private BookingService bookingService;

    @Before
    public void setUp() {

        final String baseUrl = String.format("http://localhost:%s", wireMockRule.port());

        bookingService = new BookingService(
                new PayBuddyGateway(baseUrl)
        );
    }

    @Test
    public void shouldPayForBookingSuccessfully() {
        // Given
        stubFor(any(anyUrl()).willReturn(okJson("" +
                "{" +
                "  \"paymentId\": \"2222\"," +
                "  \"paymentResponseStatus\": \"SUCCESS\"" +
                "}")));

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
        // Given
        stubFor(any(anyUrl()).willReturn(okJson("" +
                "{" +
                "  \"paymentId\": \"7777\"," +
                "  \"paymentResponseStatus\": \"FAILED\"" +
                "}")));

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
