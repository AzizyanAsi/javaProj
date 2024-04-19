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
        name = "measurement_unit",
        uniqueConstraints = @UniqueConstraint(columnNames = {"common_code"})
)
public class MeasurementUnit extends AbstractPersistentObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "representation_symbol", length = 20)
    private String representationSymbol;

    @Column(name = "common_code", nullable = false, length = 20)
    private String commonCode;

    @Column(name = "level_cat", nullable = false, length = 20)
    private String levelCat;
}
