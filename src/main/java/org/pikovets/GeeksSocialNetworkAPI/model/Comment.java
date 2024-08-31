package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "date")
    private LocalDateTime date = LocalDateTime.now();

    @NotNull
    @Column(name = "text")
    private String text;

    @ManyToOne
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User author;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "parent_comment_id", referencedColumnName = "id")
    private Comment parentComment;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE)
    private Set<CommentLike> likes = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment comment)) return false;

        if (!id.equals(comment.id)) return false;
        if (!date.equals(comment.date)) return false;
        if (!text.equals(comment.text)) return false;
        if (!post.equals(comment.post)) return false;
        if (!author.equals(comment.author)) return false;
        return Objects.equals(parentComment, comment.parentComment);
    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + text.hashCode();
        result = 31 * result + post.hashCode();
        result = 31 * result + author.hashCode();
        result = 31 * result + (parentComment != null ? parentComment.hashCode() : 0);
        return result;
    }
}