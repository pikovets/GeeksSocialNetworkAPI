package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "date")
    private ZonedDateTime date;

    @NotNull
    @Column(name = "text")
    private String text;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id", referencedColumnName = "id")
    private Comment parentComment;

    @OneToMany(mappedBy = "comment")
    private List<CommentLike> commentLikes;
}