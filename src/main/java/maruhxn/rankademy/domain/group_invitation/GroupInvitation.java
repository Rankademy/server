package maruhxn.rankademy.domain.group_invitation;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_invitations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupInvitation extends AbstractEntity {

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "invited_at", nullable = false)
    private LocalDateTime invitedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GroupInvitationStatus status = GroupInvitationStatus.PENDING;

    public GroupInvitation(Long groupId, Long userId) {
        this.groupId = groupId;
        this.userId = userId;
        this.invitedAt = LocalDateTime.now();
        this.status = GroupInvitationStatus.PENDING;
    }

    public static GroupInvitation create(Long groupId, Long userId) {
        return new GroupInvitation(groupId, userId);
    }

    public void accept() {
        Assert.isTrue(status == GroupInvitationStatus.PENDING, "대기 중인 요청이 아닙니다.");
        this.status = GroupInvitationStatus.ACCEPTED;
    }

    public void reject() {
        Assert.isTrue(status == GroupInvitationStatus.PENDING, "대기 중인 요청이 아닙니다.");
        this.status = GroupInvitationStatus.REJECTED;
    }
}
