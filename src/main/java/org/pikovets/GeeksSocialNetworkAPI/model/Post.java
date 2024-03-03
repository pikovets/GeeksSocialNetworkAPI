package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotNull
    @Column(name = "date")
    private LocalDateTime date;

    @Size(min = 1, max = 2200, message = "The post text should contain between 0 and 2200 characters")
    @Column(name = "text")
    private String text;

    @Size(max = 255, message = "The photo link must be under 255 characters")
    @Column(name = "photo_link")
    private String photoLink;

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private Set<PostLike> likes;

    @ManyToOne
    @JoinColumn(name = "community_id", referencedColumnName = "id")
    private Community community;
  
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private Set<Comment> comments;

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", date=" + date +
                ", text='" + text + '\'' +
                ", photoLink='" + photoLink + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post post)) return false;

        if (!date.equals(post.date)) return false;
        if (!Objects.equals(text, post.text)) return false;
        if (!Objects.equals(photoLink, post.photoLink)) return false;
        return author.equals(post.author);
    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (photoLink != null ? photoLink.hashCode() : 0);
        result = 31 * result + author.hashCode();
        return result;
    }
}