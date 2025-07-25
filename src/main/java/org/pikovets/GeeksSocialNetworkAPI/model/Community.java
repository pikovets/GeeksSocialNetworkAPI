package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityCategory;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.JoinType;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.PublishPermission;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@Data
@Table("community")
public class Community {
    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("category")
    private CommunityCategory category;

    @Column("photo_link")
    private String photoLink;

    @Column("publish_permission")
    @NotNull
    private PublishPermission publishPermission;

    @Column("join_type")
    @NotNull
    private JoinType joinType;

    @Column("created_date")
    private LocalDate createdDate;
}