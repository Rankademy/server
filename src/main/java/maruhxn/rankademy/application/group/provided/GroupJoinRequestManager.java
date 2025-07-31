package maruhxn.rankademy.application.group.provided;

public interface GroupJoinRequestManager {

    void addJoinRequest(Long userId, Long groupId);

    void acceptJoinRequest(Long userId, Long groupId);

    void rejectJoinRequest(Long userId, Long groupId);
}
