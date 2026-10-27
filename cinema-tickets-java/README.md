# Cinema Tickets Service

This repository provides an implementation of the Cinema Tickets service for the UK Department for Work and Pensions (DWP) coding assessment.

## Project Purpose

The service provides a ticket purchasing mechanism for cinema tickets, calculating the total price and reserving the required number of seats through third-party services while strictly enforcing cinema ticket purchasing rules and business constraints.

## Prerequisites

- **Java Development Kit (JDK)**: Java 21 or higher
- **Build Tool**: Apache Maven 3.8 or higher

## Building and Testing

### Build the Project

To compile the source code:

```bash
mvn clean compile
```

To package the JAR:

```bash
mvn clean package
```

### Run Automated Tests

To execute the full JUnit 5 test suite:

```bash
mvn clean test
```

## Business Rules and Pricing

### Ticket Types and Pricing

| Ticket Type | Price | Allocated Seat |
| ----------- | ----- | -------------- |
| **ADULT**   | £25   | Yes (1 seat)   |
| **CHILD**   | £15   | Yes (1 seat)   |
| **INFANT**  | £0    | No (0 seats)   |

### Business Constraints

1. **Ticket Quantity Limit**:
   - A single purchase may contain a maximum of **25 tickets** in total.
   - All ticket types (Adult, Child, Infant) count towards this 25-ticket maximum limit.
   - Requests exceeding 25 tickets are rejected.

2. **Adult Requirement**:
   - Child and Infant tickets cannot be purchased without at least one Adult ticket.
   - If any Child or Infant tickets are requested, the purchase must contain at least one Adult ticket.

3. **Payment Calculation**:
   - Total payment = `(adultCount * 25) + (childCount * 15) + (infantCount * 0)`.
   - The computed integer amount is transferred to `TicketPaymentService.makePayment(accountId, totalAmountToPay)`.

4. **Seat Reservation**:
   - Total seats = `adultCount + childCount`.
   - Infants do not occupy a seat (they sit on an adult's lap).
   - The computed seat count is submitted to `SeatReservationService.reserveSeat(accountId, totalSeatsToAllocate)`.

5. **Account Validation**:
   - Account IDs must be greater than `0`.
   - Accounts with ID `<= 0` or `null` are rejected.

6. **Invalid Requests**:
   - Any invalid request triggers an `InvalidPurchaseException`.
   - Neither payment nor seat reservation is called if request validation fails.
   - Handled invalid cases include: null or non-positive account IDs, null request arrays, empty requests, null elements, null ticket types, negative quantities, zero total tickets, requests exceeding 25 tickets, and Child/Infant tickets without an Adult.

## Implementation Architecture and Decisions

- **Preservation of Interfaces & Third-Party Code**:
  - `TicketService.java` is unmodified.
  - Third-party packages (`thirdparty.paymentgateway.*` and `thirdparty.seatbooking.*`) are unmodified.

- **Immutability**:
  - `TicketTypeRequest` fields (`type` and `noOfTickets`) are declared `private` and `final`, preventing post-construction mutation while preserving constructors and accessors.

- **Dependency Injection & Testability**:
  - `TicketServiceImpl` supports constructor injection for `TicketPaymentService` and `SeatReservationService`, allowing unit testing with Mockito.
  - A no-argument constructor is also provided, defaulting to the third-party implementations for backwards compatibility.

- **Execution Ordering & Failure Handling**:
  1. Validate account ID.
  2. Aggregate and validate ticket requests (counts, limits, adult requirement).
  3. Calculate total payment and required seats.
  4. Make payment via `TicketPaymentService`.
  5. Reserve seats via `SeatReservationService`.
  - As noted in the assessment specification, third-party calls are not inherently transactional; if payment fails, seat reservation is not called and the failure propagates.

- **Exceptions**:
  - `InvalidPurchaseException` extends `RuntimeException` and has been updated with overloaded constructors (`String message`, `Throwable cause`) to provide descriptive failure messages.
