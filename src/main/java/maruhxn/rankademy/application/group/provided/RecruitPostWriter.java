package maruhxn.rankademy.application.group.provided;

import jakarta.validation.Valid;
import maruhxn.rankademy.domain.group.GroupRecruitmentPost;
import maruhxn.rankademy.domain.group.dto.RecruitmentPostCreateRequest;
import maruhxn.rankademy.domain.group.dto.RecruitmentPostUpdateRequest;

public interface RecruitPostWriter {

    GroupRecruitmentPost createRecruitPost(Long groupId, @Valid RecruitmentPostCreateRequest request);

    GroupRecruitmentPost updateRecruitPost(Long groupId, @Valid RecruitmentPostUpdateRequest request);

    GroupRecruitmentPost up(Long groupId);

}
