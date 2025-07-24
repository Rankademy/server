package maruhxn.rankademy.domain.group;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maruhxn.rankademy.domain.group.dto.RecruitmentPostCreateRequest;
import maruhxn.rankademy.domain.group.dto.RecruitmentPostUpdateRequest;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_recruitment_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupRecruitmentPost extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    private String content;

    @Lob
    private String requirements;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private LocalDateTime recruitmentStartDate;

    @Column(nullable = false)
    private LocalDateTime recruitmentEndDate;

    private LocalDateTime lastUppedAt;

    public GroupRecruitmentPost(String title, String content, String requirements, Integer capacity, LocalDateTime recruitmentStartDate, LocalDateTime recruitmentEndDate) {
        this.title = title;
        this.content = content;
        this.requirements = requirements;
        this.capacity = capacity;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
        this.lastUppedAt = LocalDateTime.now();
    }

    public static GroupRecruitmentPost create(RecruitmentPostCreateRequest request) {
        return new GroupRecruitmentPost(
                request.title(),
                request.content(),
                request.requirements(),
                request.capacity(),
                request.recruitmentStartDate(),
                request.recruitmentEndDate()
        );
    }

    public void update(RecruitmentPostUpdateRequest request) {
        this.title = request.title();
        this.content = request.content();
        this.requirements = request.requirements();
        this.capacity = request.capacity();
        this.recruitmentStartDate = request.recruitmentStartDate();
        this.recruitmentEndDate = request.recruitmentEndDate();
    }

    public void up(LocalDateTime uppedAt) {
        boolean isAfterOneDay = lastUppedAt != null && Duration.between(lastUppedAt, uppedAt).toHours() > 24;
        Assert.state(isAfterOneDay, "24시간 이내에는 다시 up할 수 없습니다.");
        this.lastUppedAt = uppedAt;
    }
}
