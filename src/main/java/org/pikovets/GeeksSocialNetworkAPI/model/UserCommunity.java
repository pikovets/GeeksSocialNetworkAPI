package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@IdClass(UserCommunity.UserCommunityId.class)
@Table(name = "user_community")
public class UserCommunity {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "community_id", referencedColumnName = "id")
    private Community community;

    @NotNull
    @Column(name = "user_role")
    private Role userRole;

    public static class UserCommunityId implements Serializable {
        private UUID userId;
        private UUID communityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCommunity userCommunity = (UserCommunity) o;
        return Objects.equals(user.getId(), userCommunity.user.getId()) &&
                Objects.equals(community.getId(), userCommunity.community.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getId(), community.getId());
    }
}
