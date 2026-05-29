package com.brenluz.fairshare.domain.group;

import com.brenluz.fairshare.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "group_members")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name="group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime joinedAt;
}
