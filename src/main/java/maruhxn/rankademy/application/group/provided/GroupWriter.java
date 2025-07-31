package maruhxn.rankademy.application.group.provided;

import jakarta.validation.Valid;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.dto.GroupCreateRequest;
import maruhxn.rankademy.domain.group.dto.GroupUpdateRequest;

public interface GroupWriter {

    Group create(Long leaderId, @Valid GroupCreateRequest request);

    Group update(Long groupId, @Valid GroupUpdateRequest request);

}
