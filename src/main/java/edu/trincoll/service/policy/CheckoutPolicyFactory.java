package edu.trincoll.service.policy;

import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class CheckoutPolicyFactory {

    private final Map<MembershipType, CheckoutPolicy> byType = new EnumMap<>(MembershipType.class);

    public CheckoutPolicyFactory(List<TypedCheckoutPolicy> policies) {
        for (TypedCheckoutPolicy p : policies) {
            byType.put(p.supports(), p);
        }
    }

    public CheckoutPolicy getPolicyFor(MembershipType type) {
        CheckoutPolicy policy = byType.get(type);
        if (policy == null) {
            throw new IllegalArgumentException("No CheckoutPolicy registered for membership type: " + type);
        }
        return policy;
    }
}
