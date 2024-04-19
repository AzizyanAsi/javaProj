package net.idonow.transform.professional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.transform.geo.LocationRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalRequest {

    @NotBlank(message = "{validation.empty}")
    @Size(min = 2, message = "{validation.size.min}")
    private String address;

    @Valid
    @NotNull(message = "{validation.empty}")
    private LocationRequest location;
}
