package edu.trincoll.service.policy;

import edu.trincoll.model.Member;

public interface CheckoutPolicy {
    int getMaxBooks();
    int getLoanPeriodDays();

    default boolean canCheckout(Member member) {
        return member.getBooksCheckedOut() < getMaxBooks();
    }
}
