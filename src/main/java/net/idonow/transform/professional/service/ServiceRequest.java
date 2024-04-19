package net.idonow.transform.professional.service;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.common.util.MonetaryBigDecimalDeserializer;

import java.math.BigDecimal;

import static net.idonow.common.data.NumericConstants.MAX_DECIMAL_14_2;
import static net.idonow.common.data.NumericConstants.MIN_DECIMAL_0;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @DecimalMin(value = MIN_DECIMAL_0, message = "{validation.0positive.real}")
    @DecimalMax(value = MAX_DECIMAL_14_2, message = "{validation.decimal.exceeded}")
    @NotNull(message = "{validation.empty}")
    @JsonDeserialize(using = MonetaryBigDecimalDeserializer.class)
    private BigDecimal price;

    @Positive(message = "{validation.positive}")
    @NotNull(message = "{validation.empty}")
    private Long professionId;

    @Positive(message = "{validation.positive}")
    @NotNull(message = "{validation.empty}")
    private Long measurementUnitId;
}
