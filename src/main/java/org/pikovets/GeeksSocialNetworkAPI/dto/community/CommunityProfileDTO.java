package org.pikovets.GeeksSocialNetworkAPI.dto.community;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityCategory;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.JoinType;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.PublishPermission;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityProfileDTO {
    private UUID id;

    private String name;

    private String description;

    private CommunityCategory category;

    private String photoLink;

    private PublishPermission publishPermission;

    private JoinType joinType;

    private Date createdDate;
}