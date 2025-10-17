package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import edu.trincoll.repository.BookRepository;
import edu.trincoll.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Library Facade Integration Tests")
class LibraryFacadeIntegrationTest {

    @Autowired
    private LibraryFacade libraryFacade;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Book testBook;
    private Member regularMember;
    private Member premiumMember;
    private Member studentMember;

    @BeforeEach
    void setUp() {
        // Clean up
        bookRepository.deleteAll();
        memberRepository.deleteAll();

        // Create test book
        testBook = new Book("978-0-123456-78-9", "Clean Code", "Robert Martin",
                LocalDate.of(2008, 8, 1));
        testBook.setStatus(BookStatus.AVAILABLE);
        testBook = bookRepository.save(testBook);

        // Create test members
        regularMember = new Member("John Doe", "john@example.com");
        regularMember.setMembershipType(MembershipType.REGULAR);
        regularMember.setBooksCheckedOut(0);
        regularMember = memberRepository.save(regularMember);

        premiumMember = new Member("Jane Smith", "jane@example.com");
        premiumMember.setMembershipType(MembershipType.PREMIUM);
        premiumMember.setBooksCheckedOut(0);
        premiumMember = memberRepository.save(premiumMember);

        studentMember = new Member("Bob Student", "bob@example.com");
        studentMember.setMembershipType(MembershipType.STUDENT);
        studentMember.setBooksCheckedOut(0);
        studentMember = memberRepository.save(studentMember);
    }

    @Test
    @DisplayName("Should checkout book for regular member")
    void testCheckoutBookForRegularMember() {
        // Act
        String result = libraryFacade.checkoutBook(testBook.getIsbn(), regularMember.getEmail());

        // Assert
        assertThat(result).contains("Book checked out successfully");
        assertThat(result).contains("Due date:");
        
        Book checkedOut = bookRepository.findByIsbn(testBook.getIsbn()).get();
        assertThat(checkedOut.getStatus()).isEqualTo(BookStatus.CHECKED_OUT);
        assertThat(checkedOut.getCheckedOutBy()).isEqualTo(regularMember.getEmail());
        assertThat(checkedOut.getDueDate()).isEqualTo(LocalDate.now().plusDays(14));
    }

    @Test
    @DisplayName("Should apply premium loan period")
    void testPremiumMemberGetLongerLoanPeriod() {
        // Act
        String result = libraryFacade.checkoutBook(testBook.getIsbn(), premiumMember.getEmail());

        // Assert
        Book checkedOut = bookRepository.findByIsbn(testBook.getIsbn()).get();
        assertThat(checkedOut.getDueDate()).isEqualTo(LocalDate.now().plusDays(30));
    }

    @Test
    @DisplayName("Should apply student loan period")
    void testStudentMemberGetStudentLoanPeriod() {
        // Act
        String result = libraryFacade.checkoutBook(testBook.getIsbn(), studentMember.getEmail());

        // Assert
        Book checkedOut = bookRepository.findByIsbn(testBook.getIsbn()).get();
        assertThat(checkedOut.getDueDate()).isEqualTo(LocalDate.now().plusDays(21));
    }

    @Test
    @DisplayName("Should enforce regular member checkout limit")
    void testRegularMemberCheckoutLimit() {
        // Arrange - checkout 3 books (limit for regular)
        for (int i = 0; i < 3; i++) {
            Book book = new Book("ISBN-" + i, "Book " + i, "Author " + i, LocalDate.now());
            book.setStatus(BookStatus.AVAILABLE);
            bookRepository.save(book);
            libraryFacade.checkoutBook(book.getIsbn(), regularMember.getEmail());
        }

        // Create another book
        Book fourthBook = new Book("ISBN-4", "Book 4", "Author 4", LocalDate.now());
        fourthBook.setStatus(BookStatus.AVAILABLE);
        fourthBook = bookRepository.save(fourthBook);

        // Act - try to checkout 4th book
        String result = libraryFacade.checkoutBook(fourthBook.getIsbn(), regularMember.getEmail());

        // Assert
        assertThat(result).isEqualTo("Member has reached checkout limit");
    }

    @Test
    @DisplayName("Should return book successfully")
    void testReturnBook() {
        // Arrange - checkout a book
        libraryFacade.checkoutBook(testBook.getIsbn(), regularMember.getEmail());

        // Act - return the book
        String result = libraryFacade.returnBook(testBook.getIsbn());

        // Assert
        assertThat(result).isEqualTo("Book returned successfully");
        Book returned = bookRepository.findByIsbn(testBook.getIsbn()).get();
        assertThat(returned.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(returned.getCheckedOutBy()).isNull();
        assertThat(returned.getDueDate()).isNull();
    }

    @Test
    @DisplayName("Should calculate late fee for regular member")
    void testLateFeeCalculationForRegularMember() {
        // Arrange - checkout book
        libraryFacade.checkoutBook(testBook.getIsbn(), regularMember.getEmail());
        
        // Manually set due date to 5 days ago to simulate late return
        Book book = bookRepository.findByIsbn(testBook.getIsbn()).get();
        book.setDueDate(LocalDate.now().minusDays(5));
        bookRepository.save(book);

        // Act - return late book
        String result = libraryFacade.returnBook(testBook.getIsbn());

        // Assert - 5 days * $0.50 = $2.50
        assertThat(result).contains("Late fee: $2.50");
    }

    @Test
    @DisplayName("Should not charge late fee for premium member")
    void testNoPremiumMemberLateFee() {
        // Arrange - checkout book
        libraryFacade.checkoutBook(testBook.getIsbn(), premiumMember.getEmail());
        
        // Set due date to 5 days ago
        Book book = bookRepository.findByIsbn(testBook.getIsbn()).get();
        book.setDueDate(LocalDate.now().minusDays(5));
        bookRepository.save(book);

        // Act - return late book
        String result = libraryFacade.returnBook(testBook.getIsbn());

        // Assert
        assertThat(result).isEqualTo("Book returned successfully");
        assertThat(result).doesNotContain("Late fee");
    }

    @Test
    @DisplayName("Should calculate reduced late fee for student")
    void testStudentLateFeeCalculation() {
        // Arrange - checkout book
        libraryFacade.checkoutBook(testBook.getIsbn(), studentMember.getEmail());
        
        // Set due date to 5 days ago
        Book book = bookRepository.findByIsbn(testBook.getIsbn()).get();
        book.setDueDate(LocalDate.now().minusDays(5));
        bookRepository.save(book);

        // Act - return late book
        String result = libraryFacade.returnBook(testBook.getIsbn());

        // Assert - 5 days * $0.25 = $1.25
        assertThat(result).contains("Late fee: $1.25");
    }

    @Test
    @DisplayName("Should search books by title")
    void testSearchByTitle() {
        // Act
        List<Book> results = libraryFacade.searchByTitle("Clean");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("Should search books by author")
    void testSearchByAuthor() {
        // Act
        List<Book> results = libraryFacade.searchByAuthor("Robert Martin");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAuthor()).isEqualTo("Robert Martin");
    }

    @Test
    @DisplayName("Should search books by ISBN")
    void testSearchByIsbn() {
        // Act
        var result = libraryFacade.searchByIsbn(testBook.getIsbn());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getIsbn()).isEqualTo(testBook.getIsbn());
    }

    @Test
    @DisplayName("Should generate overdue report")
    void testGenerateOverdueReport() {
        // Arrange - create an overdue book
        libraryFacade.checkoutBook(testBook.getIsbn(), regularMember.getEmail());
        Book book = bookRepository.findByIsbn(testBook.getIsbn()).get();
        book.setDueDate(LocalDate.now().minusDays(5));
        bookRepository.save(book);

        // Act
        String report = libraryFacade.generateOverdueReport();

        // Assert
        assertThat(report).contains("OVERDUE BOOKS REPORT");
        assertThat(report).contains("Clean Code");
    }

    @Test
    @DisplayName("Should generate availability report")
    void testGenerateAvailabilityReport() {
        // Act
        String report = libraryFacade.generateAvailabilityReport();

        // Assert
        assertThat(report).contains("Available books: 1");
    }

    @Test
    @DisplayName("Should generate members report")
    void testGenerateMembersReport() {
        // Act
        String report = libraryFacade.generateMembersReport();

        // Assert
        assertThat(report).contains("Total members: 3");
    }

    @Test
    @DisplayName("Should use generic report generation")
    void testGenericReportGeneration() {
        // Act
        String overdueReport = libraryFacade.generateReport("overdue");
        String availReport = libraryFacade.generateReport("available");
        String memberReport = libraryFacade.generateReport("members");

        // Assert
        assertThat(overdueReport).contains("OVERDUE");
        assertThat(availReport).contains("Available books");
        assertThat(memberReport).contains("Total members");
    }
}
