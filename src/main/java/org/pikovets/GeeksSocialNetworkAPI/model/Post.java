package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table("post")
public class Post {
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("date")
    private LocalDateTime date;

    @Size(min = 1, max = 2200, message = "The post text should contain between 1 and 2200 characters")
    @Column("text")
    private String text;

    @Size(max = 255, message = "The photo link must be under 255 characters")
    @Column("photo_link")
    private String photoLink;

    @NotNull
    @Column("author_id")
    private UUID authorId;

    @NotNull
    @Column("community_id")
    private UUID communityId;
}