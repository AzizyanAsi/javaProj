package net.idonow.transform.professional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalSelfResponse extends ProfessionalResponse {
    private BigDecimal balance;
    private LocalDateTime created;
    private LocalDateTime updated;
}
