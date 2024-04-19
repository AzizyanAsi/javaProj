package net.idonow.transform.professional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalUpdateRequest {

    @Size(min = 2, max = 1000, message = "{validation.size.range}")
    private String selfDescription;

    @NotBlank(message = "{validation.empty}")
    @Size(min = 2, message = "{validation.size.min}")
    private String address;

    @NotNull(message = "{validation.empty}")
    private LocalTime workStartTime;

    @NotNull(message = "{validation.empty}")
    private LocalTime workEndTime;

    private boolean workingInWeekend;
}
