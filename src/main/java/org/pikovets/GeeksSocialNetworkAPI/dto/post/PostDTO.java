package org.pikovets.GeeksSocialNetworkAPI.dto.post;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PostDTO {
    private UUID id;

    @NotEmpty(message = "The post text cannot be blank")
    @Size(min = 1, max = 2200, message = "The post text should contain between 0 and 2200 characters")
    private String text;

    @NotNull
    private LocalDateTime date;
}