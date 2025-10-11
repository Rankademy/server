package maruhxn.rankademy.domain.group;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import maruhxn.rankademy.domain.group.dto.CreateRecruitmentPostRequest;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@ToString(callSuper = true)
@Table(name = "group_recruitment_post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupRecruitmentPost extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    private String content;

    @Lob
    private String requirements;

    private LocalDateTime lastUppedAt;

    private LocalDateTime createdAt;

    private boolean isActive;

    public GroupRecruitmentPost(String title, String content, String requirements) {
        this.title = title;
        this.content = content;
        this.requirements = requirements;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public static GroupRecruitmentPost create(CreateRecruitmentPostRequest request) {
        return new GroupRecruitmentPost(
                request.title(),
                request.content(),
                request.requirements()
        );
    }

    public void up(LocalDateTime uppedAt) {
        if (lastUppedAt != null) {
            boolean isAfterOneDay = Duration.between(lastUppedAt, uppedAt).toHours() > 24;
            Assert.state(isAfterOneDay, "24시간 이내에는 다시 up할 수 없습니다.");
        }
        this.lastUppedAt = uppedAt;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void update(CreateRecruitmentPostRequest request) {
        this.title = request.title();
        this.content = request.content();
        this.requirements = request.requirements();
    }
}
