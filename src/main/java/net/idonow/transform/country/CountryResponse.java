package net.idonow.transform.country;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CountryResponse {
    private Long id;
    private String name;
    private String countryCode;
    private Long currencyId;
}
