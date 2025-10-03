package maruhxn.rankademy.application.group;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.provided.GroupRecruitmentManager;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.domain.group.Group;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupRecruitmentService implements GroupRecruitmentManager {

    private final GroupReader groupReader;
    private final GroupRepository groupRepository;

    @Override
    public Group startRecruitment(Long groupId) {
        Group group = groupReader.get(groupId);
        group.startRecruitment();
        return groupRepository.save(group);
    }

    @Override
    public Group closeRecruitment(Long groupId) {
        Group group = groupReader.get(groupId);
        group.closeRecruitment();
        return groupRepository.save(group);
    }
}
