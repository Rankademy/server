package maruhxn.rankademy.adapter.persistence.ranking;

import com.querydsl.core.types.dsl.BooleanExpression;
import maruhxn.rankademy.domain.user.LolPosition;

import static maruhxn.rankademy.domain.user.QUser.user;

public class WhereClauseHelper {

    public static BooleanExpression filteredByMainPosition(LolPosition mainPosition) {
        return mainPosition == null ? null : user.mainPosition.eq(mainPosition);
    }

    public static BooleanExpression filteredByAdmissionYear(Integer admissionYear) {
        return admissionYear == null ? null : user.univInfo.admissionYear.eq(admissionYear);
    }

    public static BooleanExpression filteredByMajor(String major) {
        return major == null ? null : user.univInfo.major.eq(major);
    }
}
