package com.transactionmanager.enums;

/**
 * Valid transaction type identifiers.
 * The incoming string is validated against these values in the request DTO.
 */
public enum TransactionType {
    CARS,
    SHOPPING,
    FOOD,
    SALARY,
    TRAVEL,
    ENTERTAINMENT,
    HEALTH,
    OTHER;

    /**
     * Case-insensitive lookup — allows clients to send "cars", "CARS", "Cars", etc.
     *
     * @param value raw string from the request
     * @return matching enum constant
     * @throws IllegalArgumentException if no match is found
     */
    public static TransactionType fromString(String value) {
        try {
            return TransactionType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid transaction type: '" + value + "'. Valid values: " + java.util.Arrays.toString(values()));
        }
    }
}
