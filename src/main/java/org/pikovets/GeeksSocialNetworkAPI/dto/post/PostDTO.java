package org.pikovets.GeeksSocialNetworkAPI.dto.post;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {
    private UUID id;

    @Size(min = 1, max = 2200, message = "The post text should contain between 0 and 2200 characters")
    private String text;

    @Size(max = 255, message = "The photo link must be under 255 characters")
    private String photoLink;

    private LocalDateTime date;

    private UUID authorId;

    private UUID communityId;
}