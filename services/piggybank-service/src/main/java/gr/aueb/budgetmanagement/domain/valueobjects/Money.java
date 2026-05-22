package gr.aueb.budgetmanagement.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidMoneyException;

public class Money {
    private final BigDecimal value;

    public Money(BigDecimal value) {
        if (value == null) {
            throw new InvalidMoneyException("Money cannot be null");
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMoneyException("Money cannot be negative");
        }

        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Money money = (Money) o;
        return value.compareTo(money.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
