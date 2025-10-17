package edu.trincoll.service.report;

import edu.trincoll.model.BookStatus;
import edu.trincoll.repository.BookRepository;
import org.springframework.stereotype.Component;

/**
 * AvailabilityReportGenerator generates a report of available books.
 * Implements the Strategy pattern for report generation.
 */
@Component
public class AvailabilityReportGenerator implements ReportGenerator {
    private final BookRepository bookRepository;

    public AvailabilityReportGenerator(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public String generateReport() {
        long availableCount = bookRepository.countByStatus(BookStatus.AVAILABLE);
        return "Available books: " + availableCount;
    }
}

