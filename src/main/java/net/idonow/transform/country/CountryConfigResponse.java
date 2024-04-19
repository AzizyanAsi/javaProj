package net.idonow.transform.country;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.common.util.PhoneNumberUtils.PhoneNumberData;
import net.idonow.entity.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CountryConfigResponse {

    private Long id;
    private String name;
    private String countryCode;
    private Currency currency;
    private PhoneNumberData phoneNumberData;
}
