package net.idonow.transform.profession;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionUpdateRequest {

    @Positive(message = "{validation.positive}")
    @NotNull(message = "{validation.empty}")
    private Long id;

    @NotBlank(message = "{validation.empty}")
    @Size(min = 2, message = "{validation.size.min}")
    private String professionName;

    @Positive(message = "{validation.positive}")
    @NotNull(message = "{validation.empty}")
    private Long categoryId;

}
