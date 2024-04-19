package net.idonow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.entity.templates.AbstractPersistentObject;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static net.idonow.common.data.NumericConstants.PRECISION_COMMON;
import static net.idonow.common.data.NumericConstants.SCALE_MONETARY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(
        name = "service",
        uniqueConstraints = @UniqueConstraint(columnNames = {"profession_id", "professional_id"})
)
public class Service extends AbstractPersistentObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "s_seq")
    @SequenceGenerator(name = "s_seq", sequenceName = "service_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "price", nullable = false, precision = PRECISION_COMMON, scale = SCALE_MONETARY)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profession_id", nullable = false)
    private Profession profession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "measurement_unit_id", nullable = false)
    private MeasurementUnit measurementUnit;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
}
