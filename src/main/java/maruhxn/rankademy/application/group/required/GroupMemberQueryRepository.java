package maruhxn.rankademy.application.group.required;

import maruhxn.rankademy.domain.group.GroupMember;

import java.util.List;

public interface GroupMemberQueryRepository {

    List<GroupMember> getGroupMembers(Long groupId, int page);
}
