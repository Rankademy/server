package maruhxn.rankademy.application.group;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupMemberManager;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GroupMemberService implements GroupMemberManager {

    private final UserReader userReader;
    private final GroupReader groupReader;

    @Override
    @Transactional
    public void removeMember(Long groupId, Long memberId) {
        Group group = groupReader.get(groupId);

        if(Objects.equals(group.getLeader().getId(), memberId)) {
            throw new IllegalArgumentException("자기 자신을 추방할 수 없습니다.");
        }

        User member = userReader.get(memberId);

        group.removeMember(member);
    }
}
