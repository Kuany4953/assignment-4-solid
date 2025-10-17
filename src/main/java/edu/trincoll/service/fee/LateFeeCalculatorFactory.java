package edu.trincoll.service.fee;

import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * LateFeeCalculatorFactory manages the selection of appropriate LateFeeCalculator
 * implementations based on membership type. Demonstrates the Factory pattern
 * and supports OCP by allowing new calculators to be added without modification.
 */
@Component
public class LateFeeCalculatorFactory {
    private final Map<MembershipType, LateFeeCalculator> byType = new EnumMap<>(MembershipType.class);

    public LateFeeCalculatorFactory(List<TypedLateFeeCalculator> calculators) {
        for (TypedLateFeeCalculator calc : calculators) {
            byType.put(calc.supports(), calc);
        }
    }

    /**
     * Get the appropriate LateFeeCalculator for a membership type
     */
    public LateFeeCalculator getCalculatorFor(MembershipType type) {
        LateFeeCalculator calculator = byType.get(type);
        if (calculator == null) {
            throw new IllegalArgumentException("No LateFeeCalculator registered for membership type: " + type);
        }
        return calculator;
    }
}
