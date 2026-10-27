package uk.gov.dwp.uc.pairtest;

import java.util.Objects;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

    private static final int MAX_TICKETS_PER_PURCHASE = 25;
    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;
    private static final int INFANT_TICKET_PRICE = 0;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl() {
        this(new TicketPaymentServiceImpl(), new SeatReservationServiceImpl());
    }

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = Objects.requireNonNull(ticketPaymentService, "ticketPaymentService must not be null");
        this.seatReservationService = Objects.requireNonNull(seatReservationService, "seatReservationService must not be null");
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccountId(accountId);
        TicketCounts counts = aggregateAndValidateRequests(ticketTypeRequests);

        int totalPayment = calculateTotalPayment(counts.adultCount(), counts.childCount());
        int totalSeats = calculateTotalSeats(counts.adultCount(), counts.childCount());

        ticketPaymentService.makePayment(accountId, totalPayment);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    private void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Account ID must be greater than zero.");
        }
    }

    private TicketCounts aggregateAndValidateRequests(TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("At least one ticket request must be provided.");
        }

        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request == null) {
                throw new InvalidPurchaseException("Ticket request must not be null.");
            }
            if (request.getTicketType() == null) {
                throw new InvalidPurchaseException("Ticket type must not be null.");
            }
            int quantity = request.getNoOfTickets();
            if (quantity < 0) {
                throw new InvalidPurchaseException("Ticket quantity must not be negative.");
            }
            if (quantity > MAX_TICKETS_PER_PURCHASE) {
                throw new InvalidPurchaseException("Ticket quantity cannot exceed the maximum purchase limit of " + MAX_TICKETS_PER_PURCHASE + ".");
            }

            switch (request.getTicketType()) {
                case ADULT -> adultCount += quantity;
                case CHILD -> childCount += quantity;
                case INFANT -> infantCount += quantity;
            }
        }

        int totalTickets = adultCount + childCount + infantCount;

        if (totalTickets == 0) {
            throw new InvalidPurchaseException("Purchase must contain at least one ticket.");
        }

        if (totalTickets > MAX_TICKETS_PER_PURCHASE) {
            throw new InvalidPurchaseException("Total number of tickets cannot exceed " + MAX_TICKETS_PER_PURCHASE + ".");
        }

        if ((childCount > 0 || infantCount > 0) && adultCount == 0) {
            throw new InvalidPurchaseException("Child and Infant tickets require at least one Adult ticket.");
        }

        return new TicketCounts(adultCount, childCount, infantCount);
    }

    private int calculateTotalPayment(int adultCount, int childCount) {
        return (adultCount * ADULT_TICKET_PRICE) + (childCount * CHILD_TICKET_PRICE) + (0 * INFANT_TICKET_PRICE);
    }

    private int calculateTotalSeats(int adultCount, int childCount) {
        return adultCount + childCount;
    }

    private record TicketCounts(int adultCount, int childCount, int infantCount) {
    }
}
