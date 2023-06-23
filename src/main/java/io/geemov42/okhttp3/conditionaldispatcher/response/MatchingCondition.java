package io.geemov42.okhttp3.conditionaldispatcher.response;

import io.geemov42.okhttp3.conditionaldispatcher.enums.RequestPartToTestEnum;
import lombok.Builder;
import lombok.Data;

import java.util.regex.Pattern;

import static io.geemov42.okhttp3.conditionaldispatcher.enums.RequestPartToTestEnum.HEADER;
import static io.geemov42.okhttp3.conditionaldispatcher.enums.RequestPartToTestEnum.PARAMETER;
import static io.geemov42.okhttp3.conditionaldispatcher.utils.StringUtils.requireNonNullAndNotBlank;
import static java.util.Objects.requireNonNull;

@Data
public class MatchingCondition {
    private RequestPartToTestEnum requestPartToTest;
    private String field;
    private Pattern valuePattern;

    @Builder
    public MatchingCondition(RequestPartToTestEnum requestPartToTest, String field, String valueRegex) {

        this.requestPartToTest = requireNonNull(requestPartToTest);
        requireNonNull(valueRegex);
        this.valuePattern = Pattern.compile(valueRegex);

        if (this.requestPartToTest == HEADER || this.requestPartToTest == PARAMETER) {
            this.field = requireNonNullAndNotBlank(field);
        } else {
            this.field = field;
        }
    }
}