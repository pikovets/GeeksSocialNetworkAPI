package org.pikovets.GeeksSocialNetworkAPI.dto.community;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityCategory;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.JoinType;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.PublishPermission;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityDTO {
    private UUID id;

    private String name;

    private String description;

    private CommunityCategory category;

    private String photoLink;

    private PublishPermission publishPermission;

    private JoinType joinType;
}
