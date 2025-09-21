package maruhxn.rankademy.domain.team;

import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import maruhxn.rankademy.domain.user.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("도메인 - Team")
class TeamTest {

    @Test
    @DisplayName("팀 생성에 성공한다")
    void createTeam() {
        // given
        User representative = UserFixture.createUser(999L);
        Set<TeamMember> members = TeamFixture.createTeamMembersWithReflection(representative);

        // when
        Team team = Team.create(TeamFixture.createTeamCreateRequest(representative.getId(), members));

        // then
        assertThat(team).isNotNull();
        assertThat(team.getName()).isEqualTo("test team");
        assertThat(team.getIntro()).isEqualTo("test intro");
        assertThat(team.getGroupId()).isEqualTo(1L);
        assertThat(team.getRepresentativeId()).isEqualTo(representative.getId());
        assertThat(team.getTeamMembers()).hasSize(5);
    }

    @Test
    @DisplayName("팀 멤버가 5명이 아니면 생성에 실패한다")
    void createTeam_withLessThanFiveMembers() {
        // given
        User representative = UserFixture.createUser(999L);
        Set<TeamMember> members = TeamFixture.createTeamMembersWithReflection(representative);
        members.remove(members.iterator().next()); // 4명으로 만듦

        // when & then
        assertThatThrownBy(() ->
                Team.create(TeamFixture.createTeamCreateRequest(representative.getId(), members)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("팀원 5명의 정보를 모두 입력해주세요");
    }

    @Test
    @DisplayName("팀원의 포지션이 중복되면 생성에 실패한다")
    void createTeam_withDuplicatePositions() {
        // given
        User representative = UserFixture.createUser(999L);
        Set<TeamMember> members = TeamFixture.createTeamMembersWithReflection(representative);
        Iterator<TeamMember> iterator = members.iterator();
        TeamMember firstMember = iterator.next();
        TeamMember secondMember = iterator.next();
        members.remove(firstMember);
        members.remove(secondMember);
        members.add(new TeamMember(firstMember.getUser(), LolPosition.TOP)); // TOP 포지션 중복
        members.add(new TeamMember(UserFixture.createUser("new@test.com", "new"), LolPosition.TOP));

        // when & then
        assertThatThrownBy(() ->
                Team.create(TeamFixture.createTeamCreateRequest(representative.getId(), members)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("라인은 중복될 수 없습니다");
    }

    @Test
    @DisplayName("대표자가 팀 멤버에 속해있지 않으면 생성에 실패한다")
    void createTeam_withRepresentativeNotInRoster() {
        // given
        User representative = UserFixture.createUser(999L);
        Set<TeamMember> members = TeamFixture.createTeamMembersWithReflection(representative);
        Long otherPersonId = -1L;

        // when & then
        assertThatThrownBy(() ->
                Team.create(new TeamCreateRequest(1L, "test team", "test intro", otherPersonId, members)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대표자는 팀 멤버에 속해있어야 합니다");
    }

    @Test
    @DisplayName("팀의 평균 티어를 올바르게 계산한다")
    void averageTierInfo() {
        // given
        TeamMember representative = TeamFixture.createTeamMember(1L, new TierInfo(Tier.IRON, Rank.IV, 0), LolPosition.TOP);
        Set<TeamMember> members = new HashSet<>();
        members.add(representative);
        members.add(TeamFixture.createTeamMember(2L, new TierInfo(Tier.BRONZE, Rank.IV, 0), LolPosition.JG));
        members.add(TeamFixture.createTeamMember(3L, new TierInfo(Tier.SILVER, Rank.IV, 0), LolPosition.MID));
        members.add(TeamFixture.createTeamMember(4L, new TierInfo(Tier.GOLD, Rank.IV, 0), LolPosition.ADC));
        members.add(TeamFixture.createTeamMember(5L, new TierInfo(Tier.PLATINUM, Rank.IV, 0), LolPosition.SUP));

        Team team = Team.create(TeamFixture.createTeamCreateRequest(representative.getUser().getId(), members));

        // when
        TierInfo avgTier = team.averageTierInfo();

        // then
        // IRON 4, BRONZE 4, SILVER 4, GOLD 4, PLATINUM 4
        // 100 + 600 + 1100 + 1600 + 2100 = 5500
        // 5500 / 5 = 1100 -> SILVER 4
        assertThat(avgTier.getTier()).isEqualTo(Tier.SILVER);
        assertThat(avgTier.getRank()).isEqualTo(Rank.IV);
    }

    @Test
    @DisplayName("사용자가 팀의 대표인지 올바르게 확인한다")
    void isRepresentative() {
        // given
        User representative = UserFixture.createUser(999L);
        User otherUser = UserFixture.createUser("other@test.com", "other");
        Team team = TeamFixture.createTeamWithReflection(representative);

        // when
        boolean isRep = team.isRepresentative(representative.getId());
        boolean isNotRep = team.isRepresentative(otherUser.getId());

        // then
        assertThat(isRep).isTrue();
        assertThat(isNotRep).isFalse();
    }
}
