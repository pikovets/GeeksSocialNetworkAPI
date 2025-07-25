package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table("comment")
public class Comment {
    @Id
    private UUID id;

    @Column("date")
    private LocalDateTime date;

    @NotNull
    @Column("text")
    private String text;

    @NotNull
    @Column("post_id")
    private UUID postId;

    @NotNull
    @Column("user_id")
    private UUID userId;

    @NotNull
    @Column("parent_comment_id")
    private UUID parentCommentId;

    public Comment(String text, UUID parentCommentId, UUID postId, UUID userId) {
        this.text = text;
        this.parentCommentId = parentCommentId;
        this.postId = postId;
        this.userId = userId;
    }
}