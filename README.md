# Cinema Tickets Service — DWP Coding Assessment

A Java implementation of the Cinema Tickets service for the UK Department for Work and Pensions (DWP) Senior Software Engineer coding assessment.

## Project

The solution implements ticket purchase validation, payment calculation, and seat reservation while enforcing the assessment's business rules.

### Technology
- Java 21
- Apache Maven
- JUnit 5
- Mockito

## Business Rules

- Adult tickets cost £25; Child tickets cost £15; Infant tickets are free.
- A purchase may contain up to 25 tickets in total.
- Child and Infant tickets require at least one Adult ticket.
- Infants do not require seats.
- Invalid requests are rejected before payment or seat reservation.
- Payment is processed before seat reservation.

## Run the Tests

Navigate to the Java project directory:

```bash
cd cinema-tickets-java
mvn clean test
```

The solution has been tested with **41 tests passing, with zero failures or errors**.

## Project Structure

- `cinema-tickets-java/` — Java implementation, automated tests, Maven configuration, and detailed documentation.

See [`cinema-tickets-java/README.md`](cinema-tickets-java/README.md) for the implementation details, validation rules, and architecture decisions.
