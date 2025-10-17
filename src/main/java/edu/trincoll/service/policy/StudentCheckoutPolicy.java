package edu.trincoll.service.policy;

import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

@Component
public class StudentCheckoutPolicy implements TypedCheckoutPolicy {
    @Override public MembershipType supports() { return MembershipType.STUDENT; }
    @Override public int getMaxBooks() { return 5; }
    @Override public int getLoanPeriodDays() { return 21; }
}
