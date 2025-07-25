package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@Table("comment_like")
public class CommentLike {
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("comment_id")
    private UUID commentId;

    @NotNull
    @Column("user_id")
    private UUID userId;
}
