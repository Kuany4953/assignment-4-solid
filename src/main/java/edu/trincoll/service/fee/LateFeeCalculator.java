package edu.trincoll.service.fee;

/**
 * LateFeeCalculator interface defines the contract for calculating late fees.
 * Demonstrates Open-Closed Principle (OCP) - new fee structures can be added
 * without modifying existing code.
 */
public interface LateFeeCalculator {
    /**
     * Calculate late fee based on days late
     * @param daysLate Number of days the book is late
     * @return The calculated late fee
     */
    double calculateLateFee(long daysLate);
}
