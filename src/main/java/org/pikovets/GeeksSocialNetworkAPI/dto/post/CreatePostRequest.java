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
    @NotEmpty(message = "The post text cannot be blank")
    @Size(min = 1, max = 2200, message = "The post text should contain between 0 and 2200 characters")
    private String text;
}