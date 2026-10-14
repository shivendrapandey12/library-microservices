package com.shivendra.library.circulation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Circulation owns only its own data. Members and books are referenced by id,
 * because they live in other services' databases (database-per-service).
 */
@Entity
@Table(name = "issue_records")
public class IssueRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private LocalDate issuedOn;

    @Column(nullable = false)
    private LocalDate dueOn;

    private LocalDate returnedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;

    protected IssueRecord() {
    }

    public IssueRecord(Long memberId, Long bookId, LocalDate issuedOn, LocalDate dueOn) {
        this.memberId = memberId;
        this.bookId = bookId;
        this.issuedOn = issuedOn;
        this.dueOn = dueOn;
        this.status = IssueStatus.ISSUED;
    }

    public void markReturned(LocalDate date) {
        this.returnedOn = date;
        this.status = IssueStatus.RETURNED;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getBookId() {
        return bookId;
    }

    public LocalDate getIssuedOn() {
        return issuedOn;
    }

    public LocalDate getDueOn() {
        return dueOn;
    }

    public LocalDate getReturnedOn() {
        return returnedOn;
    }

    public IssueStatus getStatus() {
        return status;
    }
}
