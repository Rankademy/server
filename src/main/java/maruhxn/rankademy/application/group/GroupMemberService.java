package maruhxn.rankademy.application.group;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupMemberManager;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupMemberService implements GroupMemberManager {

    private final UserReader userReader;
    private final GroupReader groupReader;

    @Override
    @Transactional
    public void removeMember(Long groupId, Long memberId) {
        Group group = groupReader.get(groupId);
        User member = userReader.get(memberId);

        group.removeMember(member);
    }
}
