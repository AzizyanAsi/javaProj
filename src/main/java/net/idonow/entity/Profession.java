package net.idonow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.entity.templates.AbstractPersistentObject;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(
        name = "profession",
        uniqueConstraints = @UniqueConstraint(columnNames = {"profession_name", "profession_category_id"}
        )
)
public class Profession extends AbstractPersistentObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "p_seq")
    @SequenceGenerator(name = "p_seq", sequenceName = "profession_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "profession_name", nullable = false)
    private String professionName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profession_category_id")
    private ProfessionCategory professionCategory;
}
