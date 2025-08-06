package maruhxn.rankademy.domain.group;

import maruhxn.rankademy.domain.group.dto.CreateRecruitmentPostRequest;
import maruhxn.rankademy.domain.group.dto.GroupCreateRequest;
import maruhxn.rankademy.domain.user.User;
import org.springframework.test.util.ReflectionTestUtils;

import static maruhxn.rankademy.domain.user.UserFixture.*;

public class GroupFixture {

    public static Group createGroup(User leader) {
        return Group.create(
                createGroupCreateRequest(),
                leader
        );
    }

    public static Group createGroup(User leader, String groupName) {
        return Group.create(
                createGroupCreateRequest(groupName),
                leader
        );
    }

    public static GroupCreateRequest createGroupCreateRequest() {
        return createGroupCreateRequest("테스트 그룹");
    }

    public static GroupCreateRequest createGroupCreateRequest(String groupName) {
        return new GroupCreateRequest(
                groupName,
                "테스트 그룹입니다.",
                "logo.jpg"
        );
    }

    public static CreateRecruitmentPostRequest createRecruitmentRequest() {
        return CreateRecruitmentPostRequest.builder()
                .title("그룹원 모집합니다")
                .content("열정적인 그룹원을 모집합니다.")
                .requirements("티어 제한 없음")
                .build();
    }

    public static User createMember() {
        User member = createUser("member@rankademy.app", "member");
        member.enrollUnivInfo(createEnrollUnivRequest());
        member.completeUnivAuthentication();
        member.connectSummonerInfo(createSummonerInfoConnector("member-puuid"), createRiotAuthRequest("member", "KR1"));
        return member;
    }

    public static User createMember(Long id) {
        User member = createUser("member@rankademy.app", "member");
        ReflectionTestUtils.setField(member, "id", id);
        member.enrollUnivInfo(createEnrollUnivRequest());
        member.completeUnivAuthentication();
        member.connectSummonerInfo(createSummonerInfoConnector("member-puuid"), createRiotAuthRequest("member", "KR1"));
        return member;
    }

    public static User createMember(String email, String username) {
        User member = createUser(email, username);
        member.enrollUnivInfo(createEnrollUnivRequest("서울과학기술대학교", username + "@seoultech.ac.kr"));
        member.completeUnivAuthentication();
        member.connectSummonerInfo(createSummonerInfoConnector(username + "-puuid"), createRiotAuthRequest(username, "KR1"));
        return member;
    }

    public static User createLeader() {
        return createLeader("leader");
    }

    public static User createLeader(String leaderName) {
        User leader = createUser(leaderName + "@rankademy.app", leaderName);
        leader.enrollUnivInfo(createEnrollUnivRequest());
        leader.completeUnivAuthentication();
        leader.connectSummonerInfo(createSummonerInfoConnector(leaderName + "-puuid"), createRiotAuthRequest(leaderName, "KR1"));
        return leader;
    }

}
