package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("user_community")
public class UserCommunity {

    public UserCommunity(UUID userId, UUID communityId, CommunityRole userRole) {
        new UserCommunityId(userId, communityId);
        this.userRole = userRole;
    }

    @Id
    private UserCommunityId id;

    @NotNull
    @Column("user_role")
    private CommunityRole userRole;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserCommunityId {
        @Column("user_id")
        private UUID userId;
        @Column("community_id")
        private UUID communityId;
    }
}