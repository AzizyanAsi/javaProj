package net.idonow.transform.profession.category;

import jakarta.validation.constraints.NotBlank;
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
public class ProfessionCategoryRequest {

    @NotBlank(message = "{validation.empty}")
    @Size(min = 2, message = "{validation.size.min}")
    private String categoryName;

    // Parent can be null
    @Positive(message = "{validation.positive}")
    private Long parentId;

}
