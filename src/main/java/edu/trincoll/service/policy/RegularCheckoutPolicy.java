package edu.trincoll.service.policy;

import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

@Component
public class RegularCheckoutPolicy implements TypedCheckoutPolicy {
    @Override public MembershipType supports() { return MembershipType.REGULAR; }
    @Override public int getMaxBooks() { return 3; }
    @Override public int getLoanPeriodDays() { return 14; }
}
