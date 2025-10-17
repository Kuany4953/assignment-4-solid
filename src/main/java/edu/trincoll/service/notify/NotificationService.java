package edu.trincoll.service.notify;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;
import java.time.LocalDate;

public interface NotificationService {
    void sendCheckoutNotification(Member member, Book book, LocalDate dueDate);
    void sendReturnNotification(Member member, Book book, double lateFee);
}
