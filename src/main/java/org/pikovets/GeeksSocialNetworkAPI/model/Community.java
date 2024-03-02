package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.persistence.criteria.JoinType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.PublishPermission;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Data
@Table(name = "community")
public class Community {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "photoLink")
    private String photoLink;

    @Column(name = "postPermission")
    @Enumerated(EnumType.STRING)
    private PublishPermission publishPermission;

    @Column(name = "joinType")
    @Enumerated(EnumType.STRING)
    private JoinType joinType;
}