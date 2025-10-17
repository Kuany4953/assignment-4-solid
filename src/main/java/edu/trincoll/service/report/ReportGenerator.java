package edu.trincoll.service.report;

/**
 * ReportGenerator interface defines the contract for generating different types of reports.
 * Demonstrates Open-Closed Principle (OCP) and Strategy pattern - new report types
 * can be added without modifying existing code.
 */
public interface ReportGenerator {
    /**
     * Generate a report
     * @return The generated report as a String
     */
    String generateReport();
}

