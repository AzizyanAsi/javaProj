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
        name = "country",
        uniqueConstraints = @UniqueConstraint(columnNames = {"country_code"})
)
public class Country extends AbstractPersistentObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "country_code", nullable = false, length = 5)
    private String countryCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;
}
