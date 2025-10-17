package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;
import edu.trincoll.service.book.BookService;
import edu.trincoll.service.fee.LateFeeCalculator;
import edu.trincoll.service.fee.LateFeeCalculatorFactory;
import edu.trincoll.service.member.MemberService;
import edu.trincoll.service.notify.NotificationService;
import edu.trincoll.service.policy.CheckoutPolicy;
import edu.trincoll.service.policy.CheckoutPolicyFactory;
import edu.trincoll.service.report.ReportGenerator;
import edu.trincoll.service.search.BookSearchService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * LibraryFacade provides a simplified, unified API for library operations.
 * Orchestrates multiple services (BookService, MemberService, NotificationService, etc.)
 * to implement high-level library functionality.
 * 
 * Demonstrates the Facade pattern - presents a simplified interface hiding the
 * complexity of multiple service interactions.
 * 
 * AI Collaboration Summary:
 *
 * This refactoring was completed with AI assistance to demonstrate SOLID principles.
 * The codebase was refactored from a monolithic LibraryService into well-separated
 * services, each with a single responsibility.
 *
 * Key improvements:
 * - Single Responsibility: Each service handles one concern
 * - Open-Closed: Strategy patterns allow adding new behaviors without modification
 * - Dependency Inversion: Services depend on abstractions (interfaces)
 * - Interface Segregation: Services expose only relevant methods
 * - Liskov Substitution: Strategy implementations are properly substitutable
 */
@Service
public class LibraryFacade {
    private final BookService bookService;
    private final MemberService memberService;
    private final NotificationService notificationService;
    private final CheckoutPolicyFactory checkoutPolicyFactory;
    private final LateFeeCalculatorFactory lateFeeCalculatorFactory;
    private final BookSearchService bookSearchService;
    private final ReportGenerator overdueReportGenerator;
    private final ReportGenerator availabilityReportGenerator;
    private final ReportGenerator memberReportGenerator;

    public LibraryFacade(
            BookService bookService,
            MemberService memberService,
            NotificationService notificationService,
            CheckoutPolicyFactory checkoutPolicyFactory,
            LateFeeCalculatorFactory lateFeeCalculatorFactory,
            BookSearchService bookSearchService,
            List<ReportGenerator> reportGenerators) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.notificationService = notificationService;
        this.checkoutPolicyFactory = checkoutPolicyFactory;
        this.lateFeeCalculatorFactory = lateFeeCalculatorFactory;
        this.bookSearchService = bookSearchService;
        
        // Map report generators by type
        this.overdueReportGenerator = reportGenerators.stream()
                .filter(rg -> rg.getClass().getSimpleName().contains("Overdue"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("OverdueReportGenerator not found"));
        this.availabilityReportGenerator = reportGenerators.stream()
                .filter(rg -> rg.getClass().getSimpleName().contains("Availability"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AvailabilityReportGenerator not found"));
        this.memberReportGenerator = reportGenerators.stream()
                .filter(rg -> rg.getClass().getSimpleName().contains("Member"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("MemberReportGenerator not found"));
    }

    /**
     * Check out a book for a member
     */
    public String checkoutBook(String isbn, String memberEmail) {
        // Get book and member
        Book book = bookService.getBookByIsbn(isbn);
        Member member = memberService.getMemberByEmail(memberEmail);

        // Check if book is available
        if (!bookService.isAvailable(book)) {
            return "Book is not available";
        }

        // Get checkout policy for member's membership type
        CheckoutPolicy policy = checkoutPolicyFactory.getPolicyFor(member.getMembershipType());

        // Check if member can checkout more books
        if (!policy.canCheckout(member)) {
            return "Member has reached checkout limit";
        }

        // Perform checkout
        bookService.checkoutBook(book, member, policy.getLoanPeriodDays());
        memberService.incrementCheckoutCount(member);

        // Send notification
        notificationService.sendCheckoutNotification(member, book, book.getDueDate());

        return "Book checked out successfully. Due date: " + book.getDueDate();
    }

    /**
     * Return a book
     */
    public String returnBook(String isbn) {
        Book book = bookService.getBookByIsbn(isbn);

        if (book.getStatus().toString().equals("AVAILABLE")) {
            return "Book is not checked out";
        }

        String memberEmail = book.getCheckedOutBy();
        Member member = memberService.getMemberByEmail(memberEmail);

        // Calculate late fee
        double lateFee = 0.0;
        if (book.getDueDate() != null && book.getDueDate().isBefore(LocalDate.now())) {
            long daysLate = LocalDate.now().toEpochDay() - book.getDueDate().toEpochDay();
            LateFeeCalculator calculator = lateFeeCalculatorFactory.getCalculatorFor(member.getMembershipType());
            lateFee = calculator.calculateLateFee(daysLate);
        }

        // Perform return
        bookService.returnBook(book);
        memberService.decrementCheckoutCount(member);

        // Send notification
        notificationService.sendReturnNotification(member, book, lateFee);

        if (lateFee > 0) {
            return "Book returned. Late fee: $" + String.format("%.2f", lateFee);
        }

        return "Book returned successfully";
    }

    /**
     * Search books by title
     */
    public List<Book> searchByTitle(String title) {
        return bookSearchService.searchByTitle(title);
    }

    /**
     * Search books by author
     */
    public List<Book> searchByAuthor(String author) {
        return bookSearchService.searchByAuthor(author);
    }

    /**
     * Search for a book by ISBN
     */
    public Optional<Book> searchByIsbn(String isbn) {
        return bookSearchService.searchByIsbn(isbn);
    }

    /**
     * Generate overdue books report
     */
    public String generateOverdueReport() {
        return overdueReportGenerator.generateReport();
    }

    /**
     * Generate availability report
     */
    public String generateAvailabilityReport() {
        return availabilityReportGenerator.generateReport();
    }

    /**
     * Generate members report
     */
    public String generateMembersReport() {
        return memberReportGenerator.generateReport();
    }



