package maruhxn.rankademy.adapter.security.checker;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.domain.group.Group;
import org.springframework.stereotype.Component;

@Component("groupMemberChecker")
@RequiredArgsConstructor
public class GroupMemberChecker {

    private final GroupReader groupReader;

    public boolean isGroupMember(UserInfo user, Long groupId) {
        Group group = groupReader.get(groupId);
        return group.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.id()));
    }
}
