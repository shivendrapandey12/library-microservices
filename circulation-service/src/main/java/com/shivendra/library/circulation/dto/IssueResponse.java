package com.shivendra.library.circulation.dto;

import com.shivendra.library.circulation.model.IssueRecord;
import java.time.LocalDate;

public record IssueResponse(
        Long id,
        Long memberId,
        Long bookId,
        LocalDate issuedOn,
        LocalDate dueOn,
        LocalDate returnedOn,
        String status) {

    public static IssueResponse from(IssueRecord r) {
        return new IssueResponse(
                r.getId(), r.getMemberId(), r.getBookId(),
                r.getIssuedOn(), r.getDueOn(), r.getReturnedOn(), r.getStatus().name());
    }
}
