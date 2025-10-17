package edu.trincoll.service.fee;

import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

/**
 * RegularLateFeeCalculator implements LateFeeCalculator for regular members.
 * Regular members are charged $0.50 per day late.
 */
@Component
public class RegularLateFeeCalculator implements TypedLateFeeCalculator {
    @Override
    public MembershipType supports() {
        return MembershipType.REGULAR;
    }

