package edu.trincoll.service.fee;

import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

/**
 * PremiumLateFeeCalculator implements LateFeeCalculator for premium members.
 * Premium members don't pay late fees - this is a benefit of premium membership.
 */
@Component
public class PremiumLateFeeCalculator implements TypedLateFeeCalculator {
    @Override
    public MembershipType supports() {
        return MembershipType.PREMIUM;
    }
