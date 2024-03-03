package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(UserRelationship.UserRelationshipId.class)
@Table(name = "user_relationship")
public class UserRelationship {
    @Id
    @ManyToOne
    @JoinColumn(name = "requester_id", referencedColumnName = "id")
    private User requester;

    @Id
    @ManyToOne
    @JoinColumn(name = "acceptor_id", referencedColumnName = "id")
    private User acceptor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private RelationshipType type;

    public static class UserRelationshipId implements Serializable {
        private UUID requester;
        private UUID acceptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRelationship that = (UserRelationship) o;
        return Objects.equals(requester.getId(), that.requester.getId()) &&
                Objects.equals(acceptor.getId(), that.acceptor.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(requester.getId(), acceptor.getId());
    }
}
