package org.pikovets.GeeksSocialNetworkAPI.dto.comment;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateCommentRequest {
    private String text;
    private UUID parentCommentId;
}
