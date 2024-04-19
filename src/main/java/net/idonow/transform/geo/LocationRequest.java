package net.idonow.transform.geo;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    @NotNull(message = "{validation.empty}")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    @NotNull(message = "{validation.empty}")
    private Double longitude;

}
