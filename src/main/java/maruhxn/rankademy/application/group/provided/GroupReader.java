package maruhxn.rankademy.application.group.provided;

import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupRecruitmentPost;

public interface GroupReader {

    Group get(Long groupId);

    GroupRecruitmentPost getRecruitmentPost(Long groupId);
}
