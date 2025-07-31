package maruhxn.rankademy.application.group.provided;

import maruhxn.rankademy.domain.group.GroupMember;

import java.util.List;

public interface GroupMemberManager {

    List<GroupMember> getGroupMembers(Long groupId, int page);

    void removeMember(Long groupId, Long memberId);
}
