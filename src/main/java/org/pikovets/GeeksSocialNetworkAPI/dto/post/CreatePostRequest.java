package org.pikovets.GeeksSocialNetworkAPI.dto.post;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequest {
    @Size(min = 1, max = 2200, message = "The post text should contain between 0 and 2200 characters")
    private String text;

    @Size(max = 255, message = "The photo link must be under 255 characters")
    private String photoLink;
}