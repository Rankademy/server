package maruhxn.rankademy.application.group.provided;

import jakarta.validation.Valid;
import maruhxn.rankademy.domain.group.GroupRecruitmentPost;
import maruhxn.rankademy.domain.group.dto.CreateRecruitmentPostRequest;

public interface RecruitPostWriter {

    GroupRecruitmentPost upsertRecruitmentPost(Long groupId, @Valid CreateRecruitmentPostRequest request);

    GroupRecruitmentPost up(Long groupId);

}
