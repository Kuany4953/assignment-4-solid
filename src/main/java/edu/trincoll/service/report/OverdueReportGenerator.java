package edu.trincoll.service.report;

import edu.trincoll.model.Book;
import edu.trincoll.repository.BookRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

/**
 * OverdueReportGenerator generates a report of all overdue books.
 * Implements the Strategy pattern for report generation.
 */
@Component
public class OverdueReportGenerator implements ReportGenerator {
    private final BookRepository bookRepository;

    public OverdueReportGenerator(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public String generateReport() {
        List<Book> overdueBooks = bookRepository.findByDueDateBefore(LocalDate.now());
        StringBuilder report = new StringBuilder("OVERDUE BOOKS REPORT\n");
        report.append("====================\n");
        
        if (overdueBooks.isEmpty()) {
            report.append("No overdue books.\n");
        } else {
            for (Book book : overdueBooks) {
                report.append(String.format("%s by %s - Due: %s - Checked out by: %s\n",
                        book.getTitle(), book.getAuthor(), book.getDueDate(), book.getCheckedOutBy()));
            }
        }
        
        return report.toString();
    }
}

