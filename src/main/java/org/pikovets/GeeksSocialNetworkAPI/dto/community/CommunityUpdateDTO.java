package org.pikovets.GeeksSocialNetworkAPI.dto.community;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityCategory;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.JoinType;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.PublishPermission;

@Setter
@Getter
public class CommunityUpdateDTO {
    @Size(min = 1, max = 50, message = "Community name should be between 1 and 50 characters long")
    @Pattern(regexp = "^[A-Za-z-' ]+$", message = "Make sure you don't use numbers or symbols in community name")
    private String name;

    @Size(max = 150, message = "Community description should be between 0 and 150 characters long")
    private String description;

    @NotNull(message = "Category should not be null")
    private CommunityCategory category;

    private String photoLink;

    @NotNull(message = "Publish permission should not be null")
    @Enumerated(EnumType.STRING)
    private PublishPermission publishPermission;

    @NotNull(message = "Join type should not be null")
    @Enumerated(EnumType.STRING)
    private JoinType joinType;
}
