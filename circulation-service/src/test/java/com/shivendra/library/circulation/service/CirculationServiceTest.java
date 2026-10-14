package com.shivendra.library.circulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shivendra.library.circulation.client.BookInfo;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CirculationServiceTest {

    private CatalogClient catalog;
    private MemberClient members;
    private IssueRecordRepository issues;
    private CirculationService service;

    @BeforeEach
    void setUp() {
        catalog = mock(CatalogClient.class);
        members = mock(MemberClient.class);
        issues = mock(IssueRecordRepository.class);
        service = new CirculationService(catalog, members, issues);
    }

    private void activeMember(Long id) {
        when(members.getMember(id)).thenReturn(new MemberInfo(id, "Asha", true));
    }

    @Test
    void issueReservesCopyAndSavesRecord() {
        activeMember(1L);
        when(issues.countByMemberIdAndStatus(1L, IssueStatus.ISSUED)).thenReturn(0L);
        when(catalog.reserve(10L)).thenReturn(new BookInfo(10L, "Clean Code", 2));
        when(issues.save(any(IssueRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        IssueResponse response = service.issue(new IssueRequest(1L, 10L));

        assertEquals("ISSUED", response.status());
        assertEquals(LocalDate.now().plusDays(CirculationService.LOAN_DAYS), response.dueOn());
        verify(catalog).reserve(10L);
    }

    @Test
    void issueFailsWhenMemberDoesNotExist() {
        when(members.getMember(99L)).thenThrow(mock(FeignException.NotFound.class));

        assertThrows(ResourceNotFoundException.class, () -> service.issue(new IssueRequest(99L, 10L)));
        verify(catalog, never()).reserve(any());
    }

    @Test
    void issueFailsWhenMemberIsInactive() {
        when(members.getMember(1L)).thenReturn(new MemberInfo(1L, "Asha", false));

        assertThrows(BusinessRuleException.class, () -> service.issue(new IssueRequest(1L, 10L)));
        verify(catalog, never()).reserve(any());
    }

    @Test
    void issueFailsWhenMemberReachedLimit() {
        activeMember(1L);
        when(issues.countByMemberIdAndStatus(1L, IssueStatus.ISSUED))
                .thenReturn((long) CirculationService.MAX_ACTIVE_ISSUES);

        assertThrows(BusinessRuleException.class, () -> service.issue(new IssueRequest(1L, 10L)));
        verify(catalog, never()).reserve(any());
    }

    @Test
    void issueFailsWhenNoCopiesAvailable() {
        activeMember(1L);
        when(issues.countByMemberIdAndStatus(1L, IssueStatus.ISSUED)).thenReturn(0L);
        when(catalog.reserve(10L)).thenThrow(mock(FeignException.Conflict.class));

        assertThrows(BusinessRuleException.class, () -> service.issue(new IssueRequest(1L, 10L)));
        verify(issues, never()).save(any());
    }

    @Test
    void failedSaveReleasesTheReservedCopy() {
        activeMember(1L);
        when(issues.countByMemberIdAndStatus(1L, IssueStatus.ISSUED)).thenReturn(0L);
        when(catalog.reserve(10L)).thenReturn(new BookInfo(10L, "Clean Code", 2));
        when(issues.save(any(IssueRecord.class))).thenThrow(new IllegalStateException("db down"));

        assertThrows(IllegalStateException.class, () -> service.issue(new IssueRequest(1L, 10L)));
        verify(catalog).release(10L);
    }

    @Test
    void returnReleasesCopyAndMarksRecordReturned() {
        IssueRecord record = new IssueRecord(1L, 10L, LocalDate.now(), LocalDate.now().plusDays(14));
        when(issues.findById(5L)).thenReturn(Optional.of(record));
        when(issues.save(any(IssueRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        IssueResponse response = service.returnBook(5L);

        assertEquals("RETURNED", response.status());
        verify(catalog).release(10L);
    }

    @Test
    void returningTwiceIsRejected() {
        IssueRecord record = new IssueRecord(1L, 10L, LocalDate.now(), LocalDate.now().plusDays(14));
        record.markReturned(LocalDate.now());
        when(issues.findById(5L)).thenReturn(Optional.of(record));

        assertThrows(BusinessRuleException.class, () -> service.returnBook(5L));
        verify(catalog, never()).release(any());
    }
}
