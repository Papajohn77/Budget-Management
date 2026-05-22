package gr.aueb.budgetmanagement.infrastructure.persistence.converters;

import java.math.BigDecimal;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {
    @Override
    public BigDecimal convertToDatabaseColumn(Money money) {
        return money != null ? money.getValue() : null;
    }

    @Override
    public Money convertToEntityAttribute(BigDecimal amount) {
        return new Money(amount);
    }
}
