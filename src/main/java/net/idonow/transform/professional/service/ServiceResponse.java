package net.idonow.transform.professional.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {
    private BigDecimal price;
    private Long professionId;
    private Long measurementUnitId;
}
