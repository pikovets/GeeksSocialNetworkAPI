package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
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
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private Role userRole;

    public static class UserCommunityId implements Serializable {
        private UUID user;
        private UUID community;

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserCommunityId that)) return false;

            return user.equals(that.user) && community.equals(that.community);
        }

        @Override
        public int hashCode() {
            int result = user.hashCode();
            result = 31 * result + community.hashCode();
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCommunity that = (UserCommunity) o;
        return Objects.equals(user != null ? user.getId() : null, that.user != null ? that.user.getId() : null) &&
                Objects.equals(community != null ? community.getId() : null, that.community != null ? that.community.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user != null ? user.getId() : null, community != null ? community.getId() : null);
    }
}