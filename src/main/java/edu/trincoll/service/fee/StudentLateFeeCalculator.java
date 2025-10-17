package edu.trincoll.service.fee;

import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

/**
 * StudentLateFeeCalculator implements LateFeeCalculator for student members.
 * Student members are charged a reduced rate of $0.25 per day late.
 */
@Component
public class StudentLateFeeCalculator implements TypedLateFeeCalculator {
    @Override
    public MembershipType supports() {
        return MembershipType.STUDENT;
    }

    @Override
    public double calculateLateFee(long daysLate) {
        return daysLate * 0.25;
    }
}
