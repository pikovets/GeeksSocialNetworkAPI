package org.pikovets.GeeksSocialNetworkAPI.dto.community;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityCategory;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.JoinType;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.PublishPermission;

@Data
public class CreateCommunityRequest {
    private String name;

    private CommunityCategory category;

    private JoinType joinType;
}
