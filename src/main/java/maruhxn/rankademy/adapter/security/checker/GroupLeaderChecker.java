package maruhxn.rankademy.adapter.security.checker;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.user.Role;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("groupLeaderChecker")
@RequiredArgsConstructor
public class GroupLeaderChecker {

    private final GroupReader groupReader;

    public boolean isGroupLeader(UserInfo user, Long groupId) {
        Group group = groupReader.get(groupId);

        if (!user.isAuthorized()) return false;

        boolean isGroupLeader = Objects.equals(group.getLeader().getId(), user.id());
        boolean isAdmin = user.role().equals(Role.ROLE_ADMIN.name());

        return isGroupLeader || isAdmin;
    }
}
