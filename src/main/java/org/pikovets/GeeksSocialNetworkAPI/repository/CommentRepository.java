package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
}
