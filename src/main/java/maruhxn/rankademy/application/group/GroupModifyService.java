package maruhxn.rankademy.application.group;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.provided.GroupWriter;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.dto.GroupCreateRequest;
import maruhxn.rankademy.domain.group.dto.GroupUpdateRequest;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class GroupModifyService implements GroupWriter {

    private final UserReader userReader;
    private final GroupReader groupReader;
    private final GroupRepository groupRepository;

    @Override
    public Group create(Long leaderId, GroupCreateRequest request) {
        User leader = userReader.get(leaderId);
        Group group = Group.create(request, leader);
        return groupRepository.save(group);
    }

    @Override
    public Group update(Long groupId, GroupUpdateRequest request) {
        Group group = groupReader.get(groupId);
        group.updateGroupInfo(request);
        return groupRepository.save(group);
    }

    @Override
    public void delete(Long groupId) {
        Group group = groupReader.get(groupId);
        groupRepository.delete(group);
    }

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
