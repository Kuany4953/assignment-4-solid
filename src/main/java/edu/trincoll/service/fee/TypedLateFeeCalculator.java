package edu.trincoll.service.fee;

import edu.trincoll.model.MembershipType;

/**
 * TypedLateFeeCalculator extends LateFeeCalculator to add support for membership type identification.
 * This allows the factory to match calculators to specific membership types.
 */
public interface TypedLateFeeCalculator extends LateFeeCalculator {
    /**
     * Return the membership type this calculator supports
     */
    MembershipType supports();
}
