package net.idonow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.entity.templates.AbstractPersistentObject;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static net.idonow.common.data.NumericConstants.PRECISION_COMMON;
import static net.idonow.common.data.NumericConstants.SCALE_MONETARY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(
        name = "professional",
        uniqueConstraints = @UniqueConstraint(columnNames = {"resume_name"})
)
public class Professional extends AbstractPersistentObject {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "self_description")
    private String selfDescription;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "resume_name")
    private String resumeName;

    @SuppressWarnings("com.haulmont.jpb.UnsupportedTypeWithoutConverterInspection")
    @Column(name = "location")
    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    private Point location;

    // MUST NOT be used for disabling professional!
    // Value depends on account role. True for role PROFESSIONAL, false for CLIENT
    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "balance", nullable = false, precision = PRECISION_COMMON, scale = SCALE_MONETARY)
    private BigDecimal balance;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(name = "professional_id", nullable = false, updatable = false)
    private Set<Service> services;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "working_sample", joinColumns = @JoinColumn(name = "professional_id", nullable = false))
    @Column(name = "image_name", nullable = false)
    private Set<String> workingSamples;

    @Column(name = "work_start_time", nullable = false)
    private LocalTime workStartTime;

    @Column(name = "work_end_time", nullable = false)
    private LocalTime workEndTime;

    @Column(name = "weekend", nullable = false)
    private boolean workingInWeekend;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
}
