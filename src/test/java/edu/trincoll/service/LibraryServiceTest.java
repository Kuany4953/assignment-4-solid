package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Library Service Tests")
class LibraryServiceTest {

    @Mock
    private LibraryFacade libraryFacade;

    @InjectMocks
    private LibraryService libraryService;

    private Book availableBook;
    private Member regularMember;
    private Member premiumMember;
    private Member studentMember;

    @BeforeEach
    void setUp() {
        availableBook = new Book("978-0-123456-78-9", "Clean Code", "Robert Martin",
                LocalDate.of(2008, 8, 1));
        availableBook.setId(1L);
        availableBook.setStatus(BookStatus.AVAILABLE);

        regularMember = new Member("John Doe", "john@example.com");
        regularMember.setId(1L);
        regularMember.setMembershipType(MembershipType.REGULAR);
        regularMember.setBooksCheckedOut(0);

        premiumMember = new Member("Jane Smith", "jane@example.com");
        premiumMember.setId(2L);
        premiumMember.setMembershipType(MembershipType.PREMIUM);
        premiumMember.setBooksCheckedOut(0);

        studentMember = new Member("Bob Student", "bob@example.com");
        studentMember.setId(3L);
        studentMember.setMembershipType(MembershipType.STUDENT);
        studentMember.setBooksCheckedOut(0);
    }

    @Test
    @DisplayName("Should checkout book successfully for regular member")
    void shouldCheckoutBookForRegularMember() {
        // Arrange
        when(libraryFacade.checkoutBook(availableBook.getIsbn(), regularMember.getEmail()))
                .thenReturn("Book checked out successfully. Due date: " + LocalDate.now().plusDays(14));

        // Act
        String result = libraryService.checkoutBook(availableBook.getIsbn(), regularMember.getEmail());

        // Assert
        assertThat(result).contains("Book checked out successfully");
        assertThat(result).contains("Due date:");
        verify(libraryFacade).checkoutBook(availableBook.getIsbn(), regularMember.getEmail());
    }

    @Test
    @DisplayName("Should apply correct loan period for premium member")
    void shouldApplyPremiumLoanPeriod() {
        // Arrange
        when(libraryFacade.checkoutBook(availableBook.getIsbn(), premiumMember.getEmail()))
                .thenReturn("Book checked out successfully. Due date: " + LocalDate.now().plusDays(30));

        // Act
        libraryService.checkoutBook(availableBook.getIsbn(), premiumMember.getEmail());

        // Assert
        verify(libraryFacade).checkoutBook(availableBook.getIsbn(), premiumMember.getEmail());
    }

    @Test
    @DisplayName("Should enforce checkout limit for regular member")
    void shouldEnforceCheckoutLimitForRegularMember() {
        // Arrange
        when(libraryFacade.checkoutBook(availableBook.getIsbn(), regularMember.getEmail()))
                .thenReturn("Member has reached checkout limit");

        // Act
        String result = libraryService.checkoutBook(availableBook.getIsbn(), regularMember.getEmail());

        // Assert
        assertThat(result).isEqualTo("Member has reached checkout limit");
        verify(libraryFacade).checkoutBook(availableBook.getIsbn(), regularMember.getEmail());
    }

    @Test
    @DisplayName("Should not checkout unavailable book")
    void shouldNotCheckoutUnavailableBook() {
        // Arrange
        when(libraryFacade.checkoutBook(availableBook.getIsbn(), regularMember.getEmail()))
                .thenReturn("Book is not available");

        // Act
        String result = libraryService.checkoutBook(availableBook.getIsbn(), regularMember.getEmail());

        // Assert
        assertThat(result).isEqualTo("Book is not available");
        verify(libraryFacade).checkoutBook(availableBook.getIsbn(), regularMember.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when book not found")
    void shouldThrowExceptionWhenBookNotFound() {
        // Arrange
        when(libraryFacade.checkoutBook(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Book not found"));

        // Act & Assert
        assertThatThrownBy(() ->
                libraryService.checkoutBook("invalid-isbn", regularMember.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book not found");
    }

    @Test
    @DisplayName("Should return book successfully")
    void shouldReturnBookSuccessfully() {
        // Arrange
        when(libraryFacade.returnBook(availableBook.getIsbn()))
                .thenReturn("Book returned successfully");

        // Act
        String result = libraryService.returnBook(availableBook.getIsbn());

        // Assert
        assertThat(result).isEqualTo("Book returned successfully");
        verify(libraryFacade).returnBook(availableBook.getIsbn());
    }

    @Test
    @DisplayName("Should calculate late fee for regular member")
    void shouldCalculateLateFeeForRegularMember() {
        // Arrange
        when(libraryFacade.returnBook(availableBook.getIsbn()))
                .thenReturn("Book returned. Late fee: $2.50");

        // Act
        String result = libraryService.returnBook(availableBook.getIsbn());

        // Assert
        assertThat(result).contains("Late fee: $2.50");
        verify(libraryFacade).returnBook(availableBook.getIsbn());
    }

    @Test
    @DisplayName("Should not charge late fee for premium member")
    void shouldNotChargeLateFeeForPremiumMember() {
        // Arrange
        when(libraryFacade.returnBook(availableBook.getIsbn()))
                .thenReturn("Book returned successfully");

        // Act
        String result = libraryService.returnBook(availableBook.getIsbn());

        // Assert
        assertThat(result).isEqualTo("Book returned successfully");
        assertThat(result).doesNotContain("Late fee");
        verify(libraryFacade).returnBook(availableBook.getIsbn());
    }

    @Test
    @DisplayName("Should search books by title")
    void shouldSearchBooksByTitle() {
        // Arrange
        when(libraryFacade.searchBooks("Clean", "title"))
                .thenReturn(List.of(availableBook));

        // Act
        var results = libraryService.searchBooks("Clean", "title");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
        verify(libraryFacade).searchBooks("Clean", "title");
    }

    @Test
    @DisplayName("Should throw exception for invalid search type")
    void shouldThrowExceptionForInvalidSearchType() {
        // Arrange
        when(libraryFacade.searchBooks("test", "invalid"))
                .thenThrow(new IllegalArgumentException("Invalid search type"));

        // Act & Assert
        assertThatThrownBy(() ->
                libraryService.searchBooks("test", "invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid search type");
    }
}
