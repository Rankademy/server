package maruhxn.rankademy.domain.scrim_team;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import maruhxn.rankademy.domain.scrim_team.dto.ScrimTeamCreateRequest;
import maruhxn.rankademy.domain.scrim_team.dto.ScrimTeamUpdateRequest;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Table(name = "scrim_teams")
@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScrimTeam extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String intro;

    @Column(nullable = false)
    private Long representativeId;

    @OneToMany(mappedBy = "scrimTeam", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ScrimTeamMember> members = new HashSet<>();

    private LocalDateTime createdAt;

    private boolean isActive;

    public ScrimTeam(String name, String intro, Long representativeId) {
        this.name = name;
        this.intro = intro;
        this.representativeId = representativeId;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public void setScrimTeamMembers(Set<ScrimTeamMember> members) {
        Assert.state(members != null && members.size() == 5, "팀원 5명의 정보를 모두 입력해주세요");
        Assert.state(checkPositionDuplication(members), "라인은 중복될 수 없습니다");
        Assert.state(members.stream().anyMatch(slot -> slot.getUser().getId().equals(representativeId)), "대표자는 팀 멤버에 속해있어야 합니다");
        this.members = members;
        members.forEach(stm -> stm.setScrimTeam(this));
    }

    private static boolean checkPositionDuplication(Set<ScrimTeamMember> roster) {
        return roster.stream()
                .map(ScrimTeamMember::getPosition)
                .collect(Collectors.toSet())
                .size() == 5;
    }

    // === 도메인 메서드 ===
    public static ScrimTeam create(ScrimTeamCreateRequest createRequest) {
        ScrimTeam scrimTeam = new ScrimTeam(
                createRequest.name(),
                createRequest.intro(),
                createRequest.representativeId());

        scrimTeam.setScrimTeamMembers(createRequest.members());
        return scrimTeam;
    }

    public void update(ScrimTeamUpdateRequest updateRequest) {
        this.name = updateRequest.name();
        this.intro = updateRequest.intro();
    }

    public boolean isRepresentative(Long userId) {
        return representativeId.equals(userId);
    }

}
