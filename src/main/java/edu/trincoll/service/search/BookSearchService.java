package edu.trincoll.service.search;

import edu.trincoll.model.Book;
import edu.trincoll.repository.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * BookSearchService handles all book search operations.
 * Demonstrates Interface Segregation Principle (ISP) by providing focused search methods.
 * Clients depend only on the search methods they need, not on unrelated operations.
 */
@Service
public class BookSearchService {
    private final BookRepository bookRepository;

    public BookSearchService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Search for books by title (case-insensitive)
     */
    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Search for books by author
     */
    public List<Book> searchByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    /**
     * Search for a book by ISBN
     */
    public Optional<Book> searchByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
}
