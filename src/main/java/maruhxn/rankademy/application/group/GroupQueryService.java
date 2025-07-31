package maruhxn.rankademy.application.group;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupRecruitmentPost;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GroupQueryService implements GroupReader {

    private final GroupRepository groupRepository;

    @Override
    public Group get(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹 정보가 존재하지 않습니다. id: " + groupId));
    }

    @Override
    public GroupRecruitmentPost getRecruitmentPost(Long groupId) {
        Group group = this.get(groupId);
        return group.getRecruitmentPost();
    }
}
