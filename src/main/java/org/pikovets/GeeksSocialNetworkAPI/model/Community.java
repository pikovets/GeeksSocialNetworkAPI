package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityCategory;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.JoinType;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.PublishPermission;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Data
@Table(name = "community")
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private CommunityCategory category;

    @Column(name = "photo_link")
    private String photoLink;

    @Column(name = "publish_permission")
    @Enumerated(EnumType.STRING)
    @NotNull
    private PublishPermission publishPermission;

    @Column(name = "join_type")
    @Enumerated(EnumType.STRING)
    @NotNull
    private JoinType joinType;

    @NotNull
    @Column(name = "created_date", columnDefinition = "DATE")
    private Date createdDate;

    @OneToMany(mappedBy = "community", cascade = CascadeType.REMOVE)
    private Set<Post> posts;

    @OneToMany(mappedBy = "community", cascade = CascadeType.REMOVE)
    private Set<UserCommunity> userCommunities;
}