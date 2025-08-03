package maruhxn.rankademy.domain.group;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maruhxn.rankademy.domain.group.dto.CreateRecruitmentPostRequest;
import maruhxn.rankademy.domain.group.dto.GroupCreateRequest;
import maruhxn.rankademy.domain.group.dto.GroupUpdateRequest;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import maruhxn.rankademy.domain.user.User;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends AbstractEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Lob
    private String about;

    private String logoImage;

    @Column(nullable = false)
    private String univName;

    @Column(nullable = false)
    private int capacity = 50;

    private boolean isRecruiting;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupMember> members = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recruitment_post_id")
    private GroupRecruitmentPost recruitmentPost;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "join_requests", joinColumns = @JoinColumn(name = "group_id"))
    private Set<JoinRequest> joinRequests = new HashSet<>();

    private LocalDateTime createdAt;

    public Group(String name, String about, String logoImage, User createdBy) {
        this.name = name;
        this.about = about;
        this.logoImage = logoImage;
        this.univName = createdBy.getUnivInfo().getUnivName();
        this.addMember(createdBy, GroupRole.LEADER);
        this.createdAt = LocalDateTime.now();
        this.capacity = 50;
        this.isRecruiting = true;
    }

    public static Group create(GroupCreateRequest request, User leader) {
        Assert.state(leader.isAuthorized(), "인증된 사용자만 그룹을 생성할 수 있습니다.");

        return new Group(
                request.name(),
                request.about(),
                request.logoImage(),
                requireNonNull(leader)
        );
    }

    public void updateGroupInfo(GroupUpdateRequest request) {
        this.name = request.name();
        this.about = request.about();
        this.logoImage = request.logoImage();
    }

    public void addMember(User user, GroupRole role) {
        Assert.isTrue(this.univName.equals(user.getUnivInfo().getUnivName()), "동일한 학교 소속의 유저만 가입할 수 있습니다.");
        Assert.state(user.isAuthorized(), "인증된 사용자만 가입할 수 있습니다.");

        GroupMember groupMember = GroupMember.builder()
                .group(this)
                .user(user)
                .role(role)
                .build();

        this.members.add(groupMember);
    }

    public void removeMember(User user) {
        this.members.removeIf(member -> member.getUser().equals(user));
    }

    public void addJoinRequest(User user) {
        Assert.state(this.isRecruiting, "모집 기간이 아닙니다.");
        Assert.state(this.univName.equals(user.getUnivInfo().getUnivName()), "동일한 학교 소속의 유저만 가입을 요청할 수 있습니다.");
        Assert.state(
                this.members.stream()
                        .noneMatch(member -> member.getUser().equals(user)),
                "이미 가입된 유저입니다."
        );

        this.joinRequests.add(new JoinRequest(requireNonNull(user.getId())));
    }

    public void acceptJoinRequest(User user) {
        boolean isRequested = this.joinRequests.removeIf(request -> request.userId().equals(user.getId()));
        Assert.state(isRequested, "존재하지 않는 가입 요청입니다.");

        this.addMember(requireNonNull(user), GroupRole.MEMBER);
    }

    public void rejectJoinRequest(Long userId) {
        this.joinRequests.removeIf(request -> request.userId().equals(userId));
    }

    public GroupRecruitmentPost upsertRecruitmentPost(CreateRecruitmentPostRequest request) {
        Assert.isNull(this.recruitmentPost, "이미 모집 공고가 존재합니다.");
        this.recruitmentPost = GroupRecruitmentPost.create(request);
        return this.recruitmentPost;
    }

    public void startRecruitment() {
        this.isRecruiting = true;
        if (this.recruitmentPost != null) {
            this.recruitmentPost.activate();
        }
    }

    public void closeRecruitment() {
        this.isRecruiting = false;
        if (this.recruitmentPost != null) {
            this.recruitmentPost.deactivate();
        }
    }
}