package maruhxn.rankademy.application.group;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupJoinRequestManager;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupJoinRequestService implements GroupJoinRequestManager {

    private final UserReader userReader;
    private final GroupReader groupReader;

    @Override
    public void addJoinRequest(Long userId, Long groupId) {
        Group group = groupReader.get(groupId);
        User user = userReader.get(userId);

        group.addJoinRequest(user);
    }

    @Override
    public void acceptJoinRequest(Long userId, Long groupId) {
        Group group = groupReader.get(groupId);
        User user = userReader.get(userId);

        group.acceptJoinRequest(user);
    }

    @Override
    public void rejectJoinRequest(Long userId, Long groupId) {
        Group group = groupReader.get(groupId);

        group.rejectJoinRequest(userId);
    }
}
