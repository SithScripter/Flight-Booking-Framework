package com.demo.flightbooking.model;

/**
 * An immutable data carrier for a complete test case, including passenger and flight info.
 * Using a record significantly reduces boilerplate code.
 */
public record Passenger(
    String origin,
    String destination,
    String firstName,
    String lastName,
    String address,
    String city,
    String state,
    String zipCode,
    String cardType,
    String cardNumber,
    String month,
    String year,
    String cardName,
    int age,
    String gender
) {}