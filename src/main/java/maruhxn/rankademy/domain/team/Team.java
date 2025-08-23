package maruhxn.rankademy.domain.team;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Table(name = "teams")
@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends AbstractEntity {

    private Long groupId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String intro;

    @Column(nullable = false)
    private Long representativeId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "team_member_id")
    private Set<TeamMember> teamMembers = new HashSet<>();

    private LocalDateTime createdAt;

    public Team(Long groupId, String name, String intro, Long representativeId, Set<TeamMember> teamMembers) {
        Assert.state(teamMembers != null && teamMembers.size() == 5, "팀원 5명의 정보를 모두 입력해주세요");
        Assert.state(checkPositionDuplication(teamMembers), "라인은 중복될 수 없습니다");
//        Assert.state(checkSameGroup(roster), "모든 멤버는 동일 그룹이어야 합니다.");
        Assert.state(teamMembers.stream().anyMatch(slot -> slot.getUser().getId().equals(representativeId)), "대표자는 팀 멤버에 속해있어야 합니다");
        this.groupId = requireNonNull(groupId);
        this.name = requireNonNull(name);
        this.intro = requireNonNull(intro);
        this.representativeId = requireNonNull(representativeId);
        this.teamMembers = teamMembers;
        this.createdAt = LocalDateTime.now();
    }

    private static boolean checkPositionDuplication(Set<TeamMember> roster) {
        return roster.stream()
                .map(TeamMember::getPosition)
                .collect(Collectors.toSet())
                .size() == 5;
    }

    // === 도메인 메서드 ===
    public static Team create(Long groupId, String name, String intro, Long representativeId, Set<TeamMember> roster) {
        return new Team(groupId, name, intro, representativeId, roster);
    }

    public TierInfo averageTierInfo() {
        double mappedAvgTier = teamMembers.stream()
                .map(tm -> tm.getUser().getSummonerInfo().getTierInfo().getMappedTier())
                .mapToDouble(Double::valueOf)
                .average()
                .orElse(0.0);

        return TierMapper.scoreToTier((int) mappedAvgTier);
    }

    public boolean isRepresentative(Long userId) {
        return representativeId.equals(userId);
    }
}
