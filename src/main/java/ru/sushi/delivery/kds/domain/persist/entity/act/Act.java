package ru.sushi.delivery.kds.domain.persist.entity.act;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@SuperBuilder(toBuilder = true)
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public abstract class Act {

    private Long employeeId;

    private String name;

    @Builder.Default
    private LocalDateTime date = ZonedDateTime.now().toLocalDateTime();

    @Builder.Default
    private Instant updatedAt = ZonedDateTime.now().toInstant();

    private String updatedBy;

    @Builder.Default
    private Instant createdAt = ZonedDateTime.now().toInstant();

    private String createdBy;

    public abstract Long getId();
}
