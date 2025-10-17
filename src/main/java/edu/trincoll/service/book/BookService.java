package edu.trincoll.service.book;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.repository.BookRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * BookService handles all book-related operations.
 * Demonstrates Single Responsibility Principle (SRP) by focusing only on book operations.
 * Separates book concerns from member operations, notifications, and search functionality.
 */
@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Get a book by ISBN
     */
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
    }

    /**
     * Check out a book (update its status and checkout info)
     */
    public void checkoutBook(Book book, Member member, int loanPeriodDays) {
        book.setStatus(BookStatus.CHECKED_OUT);
        book.setCheckedOutBy(member.getEmail());
        book.setDueDate(LocalDate.now().plusDays(loanPeriodDays));
        bookRepository.save(book);
    }

    /**
     * Return a book (reset its status and checkout info)
     */
    public void returnBook(Book book) {
        book.setStatus(BookStatus.AVAILABLE);
        book.setCheckedOutBy(null);
        book.setDueDate(null);
        bookRepository.save(book);
    }

    /**
     * Check if a book is available for checkout
     */
    public boolean isAvailable(Book book) {
        return book.getStatus() == BookStatus.AVAILABLE;
    }

    /**
     * Find all books
     */
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    /**
     * Save a book
     */
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    /**
     * Delete a book by ID
     */
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    /**
     * Find books by due date before a given date (overdue books)
     */
    public List<Book> findOverdueBooks(LocalDate date) {
        return bookRepository.findByDueDateBefore(date);
    }
}
