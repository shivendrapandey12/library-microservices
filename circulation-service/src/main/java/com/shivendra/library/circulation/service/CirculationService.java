package com.shivendra.library.circulation.service;

import com.shivendra.library.circulation.client.CatalogClient;
import com.shivendra.library.circulation.client.MemberClient;
import com.shivendra.library.circulation.client.MemberInfo;
import com.shivendra.library.circulation.dto.IssueRequest;
import com.shivendra.library.circulation.dto.IssueResponse;
import com.shivendra.library.circulation.exception.BusinessRuleException;
import com.shivendra.library.circulation.exception.ResourceNotFoundException;
import com.shivendra.library.circulation.model.IssueRecord;
import com.shivendra.library.circulation.model.IssueStatus;
import com.shivendra.library.circulation.repository.IssueRecordRepository;
import feign.FeignException;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orchestrates a workflow that spans three services. There is no distributed transaction,
 * so each step that changes remote state has a compensating action if a later step fails.
 * Remote calls are deliberately kept outside any database transaction.
 */
@Service
public class CirculationService {

    static final int MAX_ACTIVE_ISSUES = 3;
    static final int LOAN_DAYS = 14;

    private static final Logger log = LoggerFactory.getLogger(CirculationService.class);

    private final CatalogClient catalogClient;
    private final MemberClient memberClient;
    private final IssueRecordRepository issues;

    public CirculationService(CatalogClient catalogClient, MemberClient memberClient, IssueRecordRepository issues) {
        this.catalogClient = catalogClient;
        this.memberClient = memberClient;
        this.issues = issues;
    }

    public IssueResponse issue(IssueRequest request) {
        MemberInfo member = loadMember(request.memberId());
        if (!member.active()) {
            throw new BusinessRuleException("Member " + request.memberId() + " is not active");
        }
        if (issues.countByMemberIdAndStatus(request.memberId(), IssueStatus.ISSUED) >= MAX_ACTIVE_ISSUES) {
            throw new BusinessRuleException("Member already has " + MAX_ACTIVE_ISSUES + " books issued");
        }

        reserveCopy(request.bookId());

        LocalDate today = LocalDate.now();
        try {
            IssueRecord saved = issues.save(
                    new IssueRecord(request.memberId(), request.bookId(), today, today.plusDays(LOAN_DAYS)));
            return IssueResponse.from(saved);
        } catch (RuntimeException ex) {
            // Compensation: give the copy back so the catalog does not lose stock.
            compensateRelease(request.bookId());
            throw ex;
        }
    }

    public IssueResponse returnBook(Long issueId) {
        IssueRecord record = issues.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue record " + issueId + " not found"));
        if (record.getStatus() == IssueStatus.RETURNED) {
            throw new BusinessRuleException("Issue record " + issueId + " is already returned");
        }

        // Release first: if the catalog is down, nothing changed here and the call can be retried.
        catalogClient.release(record.getBookId());

        record.markReturned(LocalDate.now());
        return IssueResponse.from(issues.save(record));
    }

    public List<IssueResponse> historyForMember(Long memberId) {
        return issues.findByMemberId(memberId).stream().map(IssueResponse::from).toList();
    }

    private MemberInfo loadMember(Long memberId) {
        try {
            return memberClient.getMember(memberId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Member " + memberId + " not found");
        }
    }

    private void reserveCopy(Long bookId) {
        try {
            catalogClient.reserve(bookId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Book " + bookId + " not found");
        } catch (FeignException.Conflict ex) {
            throw new BusinessRuleException("No copies available for book " + bookId);
        }
    }

    private void compensateRelease(Long bookId) {
        try {
            catalogClient.release(bookId);
        } catch (RuntimeException releaseFailure) {
            log.error("Could not release book {} after a failed issue; manual reconciliation needed",
                    bookId, releaseFailure);
        }
    }
}
