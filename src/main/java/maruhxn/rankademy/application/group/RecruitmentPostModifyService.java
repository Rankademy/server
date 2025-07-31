package maruhxn.rankademy.application.group;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.provided.RecruitPostWriter;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupRecruitmentPost;
import maruhxn.rankademy.domain.group.dto.RecruitmentPostCreateRequest;
import maruhxn.rankademy.domain.group.dto.RecruitmentPostUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class RecruitmentPostModifyService implements RecruitPostWriter {

    private final GroupReader groupReader;
    private final GroupRepository groupRepository;

    @Override
    public GroupRecruitmentPost createRecruitPost(Long groupId, RecruitmentPostCreateRequest request) {
        Group group = groupReader.get(groupId);
        GroupRecruitmentPost recruitmentPost = group.createGroupRecruitmentPost(request);
        groupRepository.save(group);
        return recruitmentPost;
    }

    @Override
    public GroupRecruitmentPost updateRecruitPost(Long groupId, RecruitmentPostUpdateRequest request) {
        Group group = groupReader.get(groupId);
        GroupRecruitmentPost recruitmentPost = group.getRecruitmentPost();
        recruitmentPost.update(request);
        groupRepository.save(group);
        return recruitmentPost;
    }

    @Override
    public GroupRecruitmentPost up(Long groupId) {
        Group group = groupReader.get(groupId);
        GroupRecruitmentPost recruitmentPost = group.getRecruitmentPost();
        recruitmentPost.up(LocalDateTime.now());
        groupRepository.save(group);
        return recruitmentPost;
    }
}
