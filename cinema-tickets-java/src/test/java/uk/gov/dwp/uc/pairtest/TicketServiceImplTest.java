package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@DisplayName("TicketServiceImpl Test Suite")
class TicketServiceImplTest {

    private static final Long VALID_ACCOUNT_ID = 1L;

    private TicketPaymentService paymentService;
    private SeatReservationService reservationService;
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        paymentService = mock(TicketPaymentService.class);
        reservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(paymentService, reservationService);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should instantiate successfully")
        void defaultConstructor_shouldInstantiateWithoutError() {
            assertDoesNotThrow(() -> new TicketServiceImpl());
        }

        @Test
        @DisplayName("Constructor should reject null payment service")
        void constructor_shouldRejectNullPaymentService() {
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new TicketServiceImpl(null, reservationService)
            );
            assertEquals("ticketPaymentService must not be null", ex.getMessage());
        }

        @Test
        @DisplayName("Constructor should reject null reservation service")
        void constructor_shouldRejectNullReservationService() {
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new TicketServiceImpl(paymentService, null)
            );
            assertEquals("seatReservationService must not be null", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Successful Purchases and Calculations")
    class SuccessfulPurchases {

        @Test
        @DisplayName("One Adult: £25 payment, 1 seat")
        void purchase_oneAdultTicket() {
            TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 1);

            ticketService.purchaseTickets(VALID_ACCOUNT_ID, request);

            verify(paymentService).makePayment(VALID_ACCOUNT_ID, 25);
            verify(reservationService).reserveSeat(VALID_ACCOUNT_ID, 1);
        }

        @Test
        @DisplayName("Multiple Adults (e.g. 4 Adults): £100 payment, 4 seats")
        void purchase_multipleAdultTickets() {
            TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 4);

            ticketService.purchaseTickets(VALID_ACCOUNT_ID, request);

            verify(paymentService).makePayment(VALID_ACCOUNT_ID, 100);
            verify(reservationService).reserveSeat(VALID_ACCOUNT_ID, 4);
        }

        @Test
        @DisplayName("One Adult and one Child: £40 payment, 2 seats")
        void purchase_oneAdultAndOneChild() {
            TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 1);
            TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 1);

            ticketService.purchaseTickets(VALID_ACCOUNT_ID, adult, child);

            verify(paymentService).makePayment(VALID_ACCOUNT_ID, 40);
            verify(reservationService).reserveSeat(VALID_ACCOUNT_ID, 2);
        }

        @Test
        @DisplayName("One Adult and one Infant: £25 payment, 1 seat (infant gets no seat and costs £0)")
        void purchase_oneAdultAndOneInfant() {
            TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 1);
            TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 1);

            ticketService.purchaseTickets(VALID_ACCOUNT_ID, adult, infant);

            verify(paymentService).makePayment(VALID_ACCOUNT_ID, 25);
            verify(reservationService).reserveSeat(VALID_ACCOUNT_ID, 1);
        }

        @Test
        @DisplayName("Two Adults, two Children, and one Infant: £80 payment, 4 seats")
        void purchase_twoAdultsTwoChildrenOneInfant() {
            TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 2);
            TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 2);
            TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 1);

            ticketService.purchaseTickets(VALID_ACCOUNT_ID, adult, child, infant);

            verify(paymentService).makePayment(VALID_ACCOUNT_ID, 80);
            verify(reservationService).reserveSeat(VALID_ACCOUNT_ID, 4);
        }

        @Test
        @DisplayName("Three Adults, two Children, and one Infant: £105 payment, 5 seats")
        void purchase_threeAdultsTwoChildrenOneInfant() {
            TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 3);
            TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 2);
            TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 1);

            ticketService.purchaseTickets(VALID_ACCOUNT_ID, adult, child, infant);

            verify(paymentService).makePayment(VALID_ACCOUNT_ID, 105);
            verify(reservationService).reserveSeat(VALID_ACCOUNT_ID, 5);
        }

        @Test
        @DisplayName("Exactly 25 Adult tickets: £625 payment, 25 seats (Maximum allowed tickets)")
        void purchase_exactlyTwentyFiveAdultTickets() {
            TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 25);

            ticketService.purchaseTickets(VALID_ACCOUNT_ID, adult);

            verify(paymentService).makePayment(VALID_ACCOUNT_ID, 625);
            verify(reservationService).reserveSeat(VALID_ACCOUNT_ID, 25);
        }

        @Test
        @DisplayName("Exactly 25 mixed tickets: 10 Adults, 10 Children, 5 Infants: £400 payment, 20 seats")
        void purchase_exactlyTwentyFiveMixedTickets() {
            TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 10);
            TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 10);
            TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 5);

            ticketService.purchaseTickets(VALID_ACCOUNT_ID, adult, child, infant);

            // (10 * 25) + (10 * 15) + (5 * 0) = 250 + 150 = 400
            verify(paymentService).makePayment(VALID_ACCOUNT_ID, 400);
            // 10 Adults + 10 Children = 20 seats
            verify(reservationService).reserveSeat(VALID_ACCOUNT_ID, 20);
        }

        @Test
        @DisplayName("Multiple requests of the same ticket type should aggregate correctly")
        void purchase_multipleRequestsSameType_shouldAggregate() {
            TicketTypeRequest adult1 = new TicketTypeRequest(Type.ADULT, 2);
            TicketTypeRequest adult2 = new TicketTypeRequest(Type.ADULT, 3);
            TicketTypeRequest child1 = new TicketTypeRequest(Type.CHILD, 1);
            TicketTypeRequest child2 = new TicketTypeRequest(Type.CHILD, 2);

            ticketService.purchaseTickets(VALID_ACCOUNT_ID, adult1, adult2, child1, child2);

            // Adults: 5 * 25 = 125, Children: 3 * 15 = 45 -> Total = 170
            verify(paymentService).makePayment(VALID_ACCOUNT_ID, 170);
            // Seats: 5 + 3 = 8
            verify(reservationService).reserveSeat(VALID_ACCOUNT_ID, 8);
        }
    }

    @Nested
    @DisplayName("Service Interaction and Ordering")
    class InteractionVerification {

        @Test
        @DisplayName("Payment must be made before seat reservation is requested")
        void paymentMustPrecedeSeatReservation() {
            TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 2);

            ticketService.purchaseTickets(VALID_ACCOUNT_ID, request);

            InOrder inOrder = inOrder(paymentService, reservationService);
            inOrder.verify(paymentService).makePayment(VALID_ACCOUNT_ID, 50);
            inOrder.verify(reservationService).reserveSeat(VALID_ACCOUNT_ID, 2);
        }

        @Test
        @DisplayName("If payment fails with an exception, seat reservation must not be called")
        void whenPaymentThrowsException_reservationShouldNotBeCalled() {
            doThrow(new RuntimeException("Payment gateway down"))
                    .when(paymentService).makePayment(anyLong(), anyInt());

            TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 1);

            assertThrows(RuntimeException.class, () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, request));

            verify(paymentService).makePayment(VALID_ACCOUNT_ID, 25);
            verifyNoInteractions(reservationService);
        }
    }

    @Nested
    @DisplayName("Account ID Validation")
    class AccountIdValidation {

        @Test
        @DisplayName("Null account ID should throw InvalidPurchaseException")
        void nullAccountId_shouldThrowException() {
            TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 1);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(null, request)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @ParameterizedTest(name = "Account ID {0} should be rejected")
        @ValueSource(longs = {0L, -1L, -100L, Long.MIN_VALUE})
        @DisplayName("Zero or negative account ID should throw InvalidPurchaseException")
        void nonPositiveAccountId_shouldThrowException(long invalidAccountId) {
            TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 1);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(invalidAccountId, request)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }
    }

    @Nested
    @DisplayName("Ticket Requests Validation")
    class TicketRequestsValidation {

        @Test
        @DisplayName("Null requests array should throw InvalidPurchaseException")
        void nullTicketRequests_shouldThrowException() {
            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, (TicketTypeRequest[]) null)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @Test
        @DisplayName("Empty requests array should throw InvalidPurchaseException")
        void emptyTicketRequests_shouldThrowException() {
            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @Test
        @DisplayName("Null element in requests array should throw InvalidPurchaseException")
        void nullTicketRequestElement_shouldThrowException() {
            TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 1);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, adult, null)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @Test
        @DisplayName("Request with null ticket type should throw InvalidPurchaseException")
        void nullTicketType_shouldThrowException() {
            TicketTypeRequest invalidRequest = new TicketTypeRequest(null, 1);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, invalidRequest)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @Test
        @DisplayName("Request with negative ticket quantity should throw InvalidPurchaseException")
        void negativeTicketQuantity_shouldThrowException() {
            TicketTypeRequest invalidRequest = new TicketTypeRequest(Type.ADULT, -1);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, invalidRequest)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @Test
        @DisplayName("Zero total tickets should throw InvalidPurchaseException")
        void zeroTotalTickets_shouldThrowException() {
            TicketTypeRequest zeroAdult = new TicketTypeRequest(Type.ADULT, 0);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, zeroAdult)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @Test
        @DisplayName("Single request with more than 25 tickets should throw InvalidPurchaseException")
        void singleRequestExceedingMaxLimit_shouldThrowException() {
            TicketTypeRequest excessiveRequest = new TicketTypeRequest(Type.ADULT, 26);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, excessiveRequest)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @Test
        @DisplayName("Combined requests with more than 25 tickets should throw InvalidPurchaseException")
        void combinedRequestsExceedingMaxLimit_shouldThrowException() {
            TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 20);
            TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 6);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, adult, child)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @Test
        @DisplayName("Combining Adults and Infants exceeding 25 tickets should throw InvalidPurchaseException")
        void infantsCountTowardMaxLimit_exceedingLimit_shouldThrowException() {
            TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 15);
            TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 11);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, adult, infant)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }
    }

    @Nested
    @DisplayName("Adult Requirement Validation")
    class AdultRequirementValidation {

        @Test
        @DisplayName("Child tickets without Adult ticket should throw InvalidPurchaseException")
        void childWithoutAdult_shouldThrowException() {
            TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 1);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, child)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @Test
        @DisplayName("Infant tickets without Adult ticket should throw InvalidPurchaseException")
        void infantWithoutAdult_shouldThrowException() {
            TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 1);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, infant)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @Test
        @DisplayName("Child and Infant tickets without Adult ticket should throw InvalidPurchaseException")
        void childAndInfantWithoutAdult_shouldThrowException() {
            TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 2);
            TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 1);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, child, infant)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }

        @Test
        @DisplayName("Request with 0 Adult tickets and 1 Child ticket should throw InvalidPurchaseException")
        void zeroAdultWithChild_shouldThrowException() {
            TicketTypeRequest zeroAdult = new TicketTypeRequest(Type.ADULT, 0);
            TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 1);

            InvalidPurchaseException ex = assertThrows(
                    InvalidPurchaseException.class,
                    () -> ticketService.purchaseTickets(VALID_ACCOUNT_ID, zeroAdult, child)
            );

            assertNotNull(ex.getMessage());
            verifyNoInteractions(paymentService, reservationService);
        }
    }
}
