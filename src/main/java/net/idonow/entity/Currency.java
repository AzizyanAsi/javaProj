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
        name = "currency",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"iso_code"}),
                @UniqueConstraint(columnNames = {"numeric_code"})
        }
)
public class Currency extends AbstractPersistentObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "iso_code", nullable = false, length = 3)
    private String isoCode;

    @Column(name = "numeric_code", nullable = false, length = 3)
    private String numericCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "symbol", length = 10)
    private String symbol;

    @Column(name = "fraction_digits", nullable = false)
    private int defaultFractionalDigits;
}

