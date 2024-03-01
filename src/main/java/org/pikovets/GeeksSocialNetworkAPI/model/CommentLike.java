package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "comment_like")
public class CommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "comment_id", referencedColumnName = "id")
    private Comment comment;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
