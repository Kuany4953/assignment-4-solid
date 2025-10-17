package edu.trincoll.service.policy;

import edu.trincoll.model.MembershipType;

public interface TypedCheckoutPolicy extends CheckoutPolicy {
    MembershipType supports();
}
