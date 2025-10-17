package edu.trincoll.service.policy;

import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

@Component
public class PremiumCheckoutPolicy implements TypedCheckoutPolicy {
    @Override public MembershipType supports() { return MembershipType.PREMIUM; }
    @Override public int getMaxBooks() { return 10; }
    @Override public int getLoanPeriodDays() { return 30; }
}
