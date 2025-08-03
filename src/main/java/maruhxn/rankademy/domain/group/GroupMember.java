package maruhxn.rankademy.domain.group;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import maruhxn.rankademy.domain.user.User;

@Entity
@Table(name = "group_member", uniqueConstraints = {
        @UniqueConstraint(
                name = "group_member_unique",
                columnNames = {"group_id", "user_id"}
        )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole role;

    public GroupMember(Group group, User user) {
        this.group = group;
        this.user = user;
        this.role = GroupRole.MEMBER;
    }

    public void setRole(GroupRole role) {
        this.role = role;
    }
}
