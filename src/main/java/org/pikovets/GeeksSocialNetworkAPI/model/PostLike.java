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
@Table("post_like")
public class PostLike {
    @Id
    @Column("id")
    private UUID id = UUID.randomUUID();

    @NotNull
    @Column("post_id")
    private UUID postId;

    @NotNull
    @Column("user_id")
    private UUID userId;

    public PostLike(UUID postId, UUID userId) {
        this.postId = postId;
        this.userId = userId;
    }
}
