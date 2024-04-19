package net.idonow.transform.profession;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionResponse {
    private Long id;
    private String professionName;
    private Long categoryId;
}
