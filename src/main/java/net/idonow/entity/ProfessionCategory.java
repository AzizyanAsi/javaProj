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
        name = "profession_category",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category_name", "parent_id"})
)
public class ProfessionCategory extends AbstractPersistentObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pc_seq")
    @SequenceGenerator(name = "pc_seq", sequenceName = "profession_cat_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    // Top level category has null parent
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private ProfessionCategory parent;
}
