package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("user_relationship")
public class UserRelationship {
    @Column("requester_id")
    private UUID requesterId;

    @Column("acceptor_id")
    private UUID acceptorId;

    @NotNull
    @Column("type")
    private RelationshipType type;
}

