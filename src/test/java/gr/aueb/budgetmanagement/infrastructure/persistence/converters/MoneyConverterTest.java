package gr.aueb.budgetmanagement.infrastructure.persistence.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidMoneyException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

class MoneyConverterTest {
    private final MoneyConverter converter = new MoneyConverter();

    @Test
    void testConvertToDatabaseColumn() {
        Money money = new Money(new BigDecimal("100.50"));
        BigDecimal dbValue = converter.convertToDatabaseColumn(money);
        assertEquals(new BigDecimal("100.50"), dbValue);
    }

    @Test
    void testConvertNullToDatabaseColumn() {
        BigDecimal dbValue = converter.convertToDatabaseColumn(null);
        assertNull(dbValue);
    }

    @Test
    void testConvertToEntityAttribute() {
        BigDecimal dbValue = new BigDecimal("100.50");
        Money money = converter.convertToEntityAttribute(dbValue);
        assertEquals(dbValue, money.getValue());
    }

    // Should never happen in practice since amount column is not nullable
    @Test
    void testConvertNullToEntityAttribute() {
        assertThrows(
            InvalidMoneyException.class,
            () -> converter.convertToEntityAttribute(null)
        );
    }
}
