package com.whatstheplan.users.model.entities;

import com.whatstheplan.users.model.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
@EqualsAndHashCode(callSuper = true, exclude = "user")
@Entity
@Table(name = "preferences")
public class Preferences extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
