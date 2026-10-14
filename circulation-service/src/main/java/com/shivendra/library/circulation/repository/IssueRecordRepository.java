package com.shivendra.library.circulation.repository;

import com.shivendra.library.circulation.model.IssueRecord;
import com.shivendra.library.circulation.model.IssueStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRecordRepository extends JpaRepository<IssueRecord, Long> {

    long countByMemberIdAndStatus(Long memberId, IssueStatus status);

    List<IssueRecord> findByMemberId(Long memberId);
}
