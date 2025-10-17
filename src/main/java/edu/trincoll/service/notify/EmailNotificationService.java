package edu.trincoll.service.notify;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class EmailNotificationService implements NotificationService {

    @Override
    public void sendCheckoutNotification(Member member, Book book, LocalDate dueDate) {
        System.out.printf("[EMAIL] To: %s | Checked out: \"%s\" | Due: %s%n",
                member.getEmail(), book.getTitle(), dueDate);
    }

    @Override
    public void sendReturnNotification(Member member, Book book, double lateFee) {
        System.out.printf("[EMAIL] To: %s | Returned: \"%s\" | Late fee: $%.2f%n",
                member.getEmail(), book.getTitle(), lateFee);
    }
}
