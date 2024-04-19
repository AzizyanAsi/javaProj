package net.idonow.transform.profession.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfessionCategoryUpdateRequest {

    @Positive(message = "{validation.positive}")
    @NotNull(message = "{validation.empty}")
    private Long id;

    @NotBlank(message = "{validation.empty}")
    @Size(min = 2, message = "{validation.size.min}")
    private String categoryName;

    // Parent can be null
    @Positive(message = "{validation.positive}")
    private Long parentId;
}
