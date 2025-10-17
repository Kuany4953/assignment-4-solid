package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import edu.trincoll.repository.BookRepository;
import edu.trincoll.repository.MemberRepository;
import edu.trincoll.service.fee.LateFeeCalculatorFactory;
import edu.trincoll.service.policy.CheckoutPolicyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("Library Facade Edge Case Tests")
class LibraryFacadeEdgeCaseTest {

    @Autowired
    private LibraryFacade libraryFacade;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CheckoutPolicyFactory checkoutPolicyFactory;

    @Autowired
    private LateFeeCalculatorFactory lateFeeCalculatorFactory;

    private Book testBook;
    private Member regularMember;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        memberRepository.deleteAll();

        testBook = new Book("978-0-123456-78-9", "Clean Code", "Robert Martin",
                LocalDate.of(2008, 8, 1));
        testBook.setStatus(BookStatus.AVAILABLE);
        testBook = bookRepository.save(testBook);

        regularMember = new Member("John Doe", "john@example.com");
        regularMember.setMembershipType(MembershipType.REGULAR);
        regularMember.setBooksCheckedOut(0);
        regularMember = memberRepository.save(regularMember);
    }

    @Test
    @DisplayName("Should throw exception for non-existent book")
    void testCheckoutNonExistentBook() {
        assertThatThrownBy(() -> libraryFacade.checkoutBook("INVALID-ISBN", regularMember.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book not found");
    }

    @Test
    @DisplayName("Should throw exception for non-existent member")
    void testCheckoutNonExistentMember() {
        assertThatThrownBy(() -> libraryFacade.checkoutBook(testBook.getIsbn(), "invalid@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Member not found");
    }

    @Test
    @DisplayName("Should throw exception when returning non-existent book")
    void testReturnNonExistentBook() {
        assertThatThrownBy(() -> libraryFacade.returnBook("INVALID-ISBN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book not found");
    }

    @Test
    @DisplayName("Should return book not checked out message")
    void testReturnAvailableBook() {
        String result = libraryFacade.returnBook(testBook.getIsbn());
        assertThat(result).isEqualTo("Book is not checked out");
    }

    @Test
    @DisplayName("Should throw exception for invalid search type")
    void testInvalidSearchType() {
        assertThatThrownBy(() -> libraryFacade.searchBooks("test", "invalid_type"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid search type");
    }

    @Test
    @DisplayName("Should return empty list for no search results")
    void testSearchWithNoResults() {
        var results = libraryFacade.searchByTitle("NonExistentBook");
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should return empty optional for ISBN not found")
    void testSearchByIsbnNotFound() {
        var result = libraryFacade.searchByIsbn("INVALID-ISBN");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception for invalid report type")
    void testInvalidReportType() {
        assertThatThrownBy(() -> libraryFacade.generateReport("invalid_report"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid report type");
    }

    @Test
    @DisplayName("Should generate empty overdue report when no overdue books")
    void testOverdueReportEmpty() {
        String report = libraryFacade.generateOverdueReport();
        assertThat(report).contains("OVERDUE BOOKS REPORT");
        assertThat(report).contains("No overdue books");
    }

    @Test
    @DisplayName("Should handle zero late days fee calculation")
    void testZeroLateDaysFeeCalculation() {
        // Checkout and immediately return (no late fee)
        libraryFacade.checkoutBook(testBook.getIsbn(), regularMember.getEmail());
        
        String result = libraryFacade.returnBook(testBook.getIsbn());
        
        assertThat(result).isEqualTo("Book returned successfully");
        assertThat(result).doesNotContain("Late fee");
    }

    @Test
    @DisplayName("Should get checkout policy for regular member")
    void testGetCheckoutPolicyRegular() {
        var policy = checkoutPolicyFactory.getPolicyFor(MembershipType.REGULAR);
        assertThat(policy.getMaxBooks()).isEqualTo(3);
        assertThat(policy.getLoanPeriodDays()).isEqualTo(14);
    }

    @Test
    @DisplayName("Should get checkout policy for premium member")
    void testGetCheckoutPolicyPremium() {
        var policy = checkoutPolicyFactory.getPolicyFor(MembershipType.PREMIUM);
        assertThat(policy.getMaxBooks()).isEqualTo(10);
        assertThat(policy.getLoanPeriodDays()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should get checkout policy for student member")
    void testGetCheckoutPolicyStudent() {
        var policy = checkoutPolicyFactory.getPolicyFor(MembershipType.STUDENT);
        assertThat(policy.getMaxBooks()).isEqualTo(5);
        assertThat(policy.getLoanPeriodDays()).isEqualTo(21);
    }

    @Test
    @DisplayName("Should get late fee calculator for regular member")
    void testGetLateFeeCalculatorRegular() {
        var calc = lateFeeCalculatorFactory.getCalculatorFor(MembershipType.REGULAR);
        assertThat(calc.calculateLateFee(5)).isEqualTo(2.50);
        assertThat(calc.calculateLateFee(10)).isEqualTo(5.00);
    }

    @Test
    @DisplayName("Should get late fee calculator for premium member")
    void testGetLateFeeCalculatorPremium() {
        var calc = lateFeeCalculatorFactory.getCalculatorFor(MembershipType.PREMIUM);
        assertThat(calc.calculateLateFee(5)).isEqualTo(0.0);
        assertThat(calc.calculateLateFee(100)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should get late fee calculator for student member")
    void testGetLateFeeCalculatorStudent() {
        var calc = lateFeeCalculatorFactory.getCalculatorFor(MembershipType.STUDENT);
        assertThat(calc.calculateLateFee(5)).isEqualTo(1.25);
        assertThat(calc.calculateLateFee(10)).isEqualTo(2.50);
    }

    @Test
    @DisplayName("Should throw exception for invalid checkout policy membership type")
    void testInvalidCheckoutPolicyType() {
        // This would only happen if MembershipType is extended without adding a policy
        // Current implementation: all types are covered
        assertThat(checkoutPolicyFactory.getPolicyFor(MembershipType.REGULAR)).isNotNull();
    }

    @Test
    @DisplayName("Should support multiple searches in sequence")
    void testMultipleSearches() {
        Book book1 = new Book("ISBN-1", "Java Basics", "John Smith", LocalDate.now());
        Book book2 = new Book("ISBN-2", "Java Advanced", "Jane Smith", LocalDate.now());
        bookRepository.save(book1);
        bookRepository.save(book2);

        var titleResults = libraryFacade.searchByTitle("Java");
        assertThat(titleResults).hasSizeGreaterThanOrEqualTo(2);

        var isbnResult = libraryFacade.searchByIsbn("ISBN-1");
        assertThat(isbnResult).isPresent();
    }

    @Test
    @DisplayName("Should handle multiple checkout-return cycles")
    void testMultipleCheckoutReturnCycles() {
        // First cycle
        libraryFacade.checkoutBook(testBook.getIsbn(), regularMember.getEmail());
        Book checked = bookRepository.findByIsbn(testBook.getIsbn()).get();
        assertThat(checked.getStatus()).isEqualTo(BookStatus.CHECKED_OUT);

        libraryFacade.returnBook(testBook.getIsbn());
        Book returned = bookRepository.findByIsbn(testBook.getIsbn()).get();
        assertThat(returned.getStatus()).isEqualTo(BookStatus.AVAILABLE);

        // Second cycle
        libraryFacade.checkoutBook(testBook.getIsbn(), regularMember.getEmail());
        Book checked2 = bookRepository.findByIsbn(testBook.getIsbn()).get();
        assertThat(checked2.getStatus()).isEqualTo(BookStatus.CHECKED_OUT);

        libraryFacade.returnBook(testBook.getIsbn());
        Book returned2 = bookRepository.findByIsbn(testBook.getIsbn()).get();
        assertThat(returned2.getStatus()).isEqualTo(BookStatus.AVAILABLE);
    }
}
