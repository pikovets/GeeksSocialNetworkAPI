package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotNull
    @Column(name = "date")
    private LocalDateTime date;

    @NotEmpty(message = "The post text cannot be blank")
    @Size(min = 1, max = 2200, message = "The post text should contain between 0 and 2200 characters")
    @Column(name = "text")
    private String text;

    @ManyToOne
    @JoinColumn(name="author_id", referencedColumnName = "id")
    private User author;
}