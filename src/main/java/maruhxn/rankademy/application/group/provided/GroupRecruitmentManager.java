package maruhxn.rankademy.application.group.provided;

import maruhxn.rankademy.domain.group.Group;

public interface GroupRecruitmentManager {

    Group startRecruitment(Long groupId);

    Group closeRecruitment(Long groupId);
}
