package gr.aueb.budgetmanagement.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidMoneyException;

class MoneyTest {
    @Test
    void constructorWithPositiveAmount() {
        Money money = new Money(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), money.getValue());
    }

    @Test
    void constructorWithNullAmount() {
        assertThrows(
            InvalidMoneyException.class,
            () -> new Money(null)
        );
    }

    @Test
    void constructorWithNegativeAmount() {
        assertThrows(
            InvalidMoneyException.class,
            () -> new Money(new BigDecimal("-100.00"))
        );
    }

    @Test
    void equalsWithSameAmount() {
        Money money1 = new Money(new BigDecimal("100.00"));
        Money money2 = new Money(new BigDecimal("100.00"));

        assertEquals(money1, money2);
    }

    @Test
    void equalsWithDifferentAmount() {
        Money money1 = new Money(new BigDecimal("100.00"));
        Money money2 = new Money(new BigDecimal("50.00"));

        assertNotEquals(money1, money2);
    }
}
