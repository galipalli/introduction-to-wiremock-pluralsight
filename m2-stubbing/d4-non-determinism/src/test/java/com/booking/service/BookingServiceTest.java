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

import static com.booking.service.BookingResponse.BookingResponseStatus.SUCCESS;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingServiceTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private BookingService bookingService;

    @Before
    public void setUp() {
        bookingService = new BookingService(new PayBuddyGateway("localhost", wireMockRule.port()));
    }

    @Test
    public void shouldPayForBookingWithOurOwnPaymentId() {
        // Given
        stubFor(post(urlPathEqualTo("/payments"))
                .withRequestBody(matchingJsonPath("$.creditCardNumber", equalTo("1234-1234-1234-1234")))
                .withRequestBody(matchingJsonPath("$.creditCardExpiry", equalTo("2018-02-01")))
                .withRequestBody(matchingJsonPath("$.amount", equalTo("20.55")))
                .withRequestBody(matchingJsonPath("$.paymentId"))
                .willReturn(
                        okJson("{" +
                                "  \"paymentResponseStatus\": \"SUCCESS\"" +
                                "}")));

        stubFor(get(urlPathEqualTo("/blacklisted-cards/1234-1234-1234-1234")).willReturn(okJson("{" +
                "  \"blacklisted\": \"false\"" +
                "}")));

        // When
        final BookingResponse bookingResponse = bookingService.payForBooking(
                new BookingPayment(
                        "1111",
                        new BigDecimal("20.55"),
                        new CreditCard("1234-1234-1234-1234", LocalDate.of(2018, 2, 1))));

        // Then
        assertThat(bookingResponse.getBookingId()).isEqualTo("1111");
        assertThat(bookingResponse.getBookingResponseStatus()).isEqualTo(SUCCESS);
    }

    @Test
    public void shouldPayForMultipleBookingsWithOurOwnPaymentId() {
        // Given
        stubFor(post(urlPathEqualTo("/payments"))
                .withRequestBody(matchingJsonPath("$.creditCardNumber"))
                .withRequestBody(matchingJsonPath("$.creditCardExpiry"))
                .withRequestBody(matchingJsonPath("$.amount"))
                .withRequestBody(matchingJsonPath("$.paymentId"))
                .willReturn(
                        okJson("{" +
                                "  \"paymentResponseStatus\": \"SUCCESS\"" +
                                "}")));

        stubFor(get(urlPathMatching("/blacklisted-cards/.*")).willReturn(okJson("{" +
                "  \"blacklisted\": \"false\"" +
                "}")));

        // When
        final BookingResponse bookingResponse = bookingService.payForBookingWithMultipleCards(
                new BookingPayment(
                        "1111",
                        new BigDecimal("20.55"),
                        new CreditCard("1234-1234-1234-1234", LocalDate.of(2018, 2, 1))));

        // Then
        assertThat(bookingResponse.getBookingId()).isEqualTo("1111");
        assertThat(bookingResponse.getBookingResponseStatus()).isEqualTo(SUCCESS);
    }
}
